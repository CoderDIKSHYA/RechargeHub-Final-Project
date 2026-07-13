FROM python:3.10-slim

WORKDIR /app

# Install dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest of the application
COPY . .

# Environment variable to point to Ollama on the host
ENV OLLAMA_URL=http://host.docker.internal:11434/api/generate

# Expose the FastAPI port
EXPOSE 8000

# Run the application
CMD ["uvicorn", "fastapi_app:app", "--host", "0.0.0.0", "--port", "8000"]
