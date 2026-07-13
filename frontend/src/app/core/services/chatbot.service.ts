import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ChatbotService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8000/api/ask';

  ask(question: string): Observable<any> {
    return this.http.post(this.apiUrl, { question });
  }
}
