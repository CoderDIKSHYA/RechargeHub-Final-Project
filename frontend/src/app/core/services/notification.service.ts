import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { NotificationResponse } from '../models/recharge.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {

  private base = `${environment.apiUrl}/api/notifications`;

  constructor(private http: HttpClient) {}

  getByUserId(userId: number): Observable<NotificationResponse[]> {
    return this.http.get<NotificationResponse[]>(`${this.base}/user/${userId}`);
  }

  getAll(): Observable<NotificationResponse[]> {
    return this.http.get<NotificationResponse[]>(this.base);
  }
}
