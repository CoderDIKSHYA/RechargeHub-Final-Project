from __future__ import annotations

import uuid
from pathlib import Path
from typing import List, Optional

import requests
from fastapi import FastAPI, File, HTTPException, Query, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import HTMLResponse
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates
from pydantic import BaseModel, Field
from starlette.requests import Request
import re

def secure_filename(filename: str) -> str:
    """Small filename sanitizer to avoid framework-specific dependency in FastAPI."""
    filename = filename.strip().replace("\\", "/").split("/")[-1]
    filename = re.sub(r"[^A-Za-z0-9_.-]", "_", filename)
    return filename or "uploaded_file"


from rag_core import (
    DEFAULT_TOP_K,
    GENERATION_MODEL,
    UPLOAD_DIR,
    allowed_file,
    ask_ollama,
    health_payload,
    index_document,
    load_index,
    reset_index,
    retrieve_context,
)

app = FastAPI(
    title="Ollama RAG FastAPI Service",
    description="A local RAG API service using uploaded .txt/.md files and Ollama.",
    version="1.0.0",
)

# Allow browser apps, React apps, .NET apps, Java apps, etc. to call this service.
# For production, replace ['*'] with your exact frontend/API domains.
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

BASE_DIR = Path(__file__).resolve().parent
app.mount("/static", StaticFiles(directory=str(BASE_DIR / "static")), name="static")
templates = Jinja2Templates(directory=str(BASE_DIR / "templates"))


class AskRequest(BaseModel):
    question: str = Field(..., min_length=1, description="Question to ask against indexed documents")
    top_k: int = Field(default=DEFAULT_TOP_K, ge=1, le=10, description="Number of chunks to retrieve")
    model: Optional[str] = Field(default=None, description="Optional Ollama model override, for example llama3.2:1b")


class AskResponse(BaseModel):
    answer: str
    sources: List[dict]


@app.get("/", response_class=HTMLResponse, tags=["UI"])
async def home(request: Request):
    stored_index = load_index()
    return templates.TemplateResponse(
        "index.html",
        {
            "request": request,
            "documents": stored_index["documents"],
            "model_name": GENERATION_MODEL,
        },
    )


@app.get("/api/health", tags=["System"])
async def api_health():
    return health_payload()


# Backward-compatible health route for the old Flask-style frontend/API users.
@app.get("/health", tags=["System"])
async def health():
    return health_payload()


@app.post("/api/upload", tags=["Documents"])
async def api_upload_file(file: UploadFile = File(...)):
    if not file.filename:
        raise HTTPException(status_code=400, detail="No file selected.")

    if not allowed_file(file.filename):
        raise HTTPException(status_code=400, detail="Only .txt and .md files are allowed.")

    filename = secure_filename(file.filename)
    unique_name = f"{uuid.uuid4().hex}_{filename}"
    save_path = UPLOAD_DIR / unique_name

    content = await file.read()
    if not content:
        raise HTTPException(status_code=400, detail="Uploaded file is empty.")

    save_path.write_bytes(content)
    chunk_count = index_document(save_path, filename)

    return {
        "message": f"Uploaded and indexed '{filename}' successfully.",
        "filename": filename,
        "stored_name": unique_name,
        "chunks": chunk_count,
    }


# Backward-compatible route used by existing static/script.js.
@app.post("/upload", tags=["Documents"])
async def upload_file(file: UploadFile = File(...)):
    return await api_upload_file(file)


@app.get("/api/documents", tags=["Documents"])
async def api_documents():
    return load_index()["documents"]


# Backward-compatible route used by existing static/script.js.
@app.get("/documents", tags=["Documents"])
async def documents():
    return load_index()["documents"]


@app.get("/api/retrieve", tags=["RAG"])
async def api_retrieve(
    question: str = Query(..., min_length=1),
    top_k: int = Query(DEFAULT_TOP_K, ge=1, le=10),
):
    return {"question": question, "sources": retrieve_context(question, top_k=top_k)}


@app.post("/api/ask", response_model=AskResponse, tags=["RAG"])
async def api_ask(payload: AskRequest):
    question = payload.question.strip()
    if not question:
        raise HTTPException(status_code=400, detail="Question is required.")

    contexts = retrieve_context(question, top_k=payload.top_k)
    try:
        answer = ask_ollama(question, contexts, model=payload.model)
    except requests.exceptions.ConnectionError:
        raise HTTPException(
            status_code=503,
            detail="Could not connect to Ollama. Start Ollama and make sure the model is available.",
        )
    except requests.exceptions.RequestException as exc:
        raise HTTPException(status_code=502, detail=f"Ollama request failed: {str(exc)}")

    return {"answer": answer, "sources": contexts}


# Backward-compatible route used by existing static/script.js.
@app.post("/ask", tags=["RAG"])
async def ask(payload: AskRequest):
    return await api_ask(payload)


@app.delete("/api/reset", tags=["Documents"])
async def api_reset(delete_uploads: bool = Query(False)):
    reset_index(delete_uploads=delete_uploads)
    return {"message": "Index reset successfully.", "delete_uploads": delete_uploads}
