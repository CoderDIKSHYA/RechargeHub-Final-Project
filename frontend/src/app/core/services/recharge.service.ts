import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { RechargeRequest, RechargeResponse } from '../models/recharge.model';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class RechargeService {

  private base = `${environment.apiUrl}/recharges`;

  constructor(private http: HttpClient, private auth: AuthService) {}

  initiate(userId: number, req: RechargeRequest): Observable<RechargeResponse> {
    return this.http.post<RechargeResponse>(this.base, req);
  }

  getByUserId(userId: number): Observable<RechargeResponse[]> {
    return this.http.get<RechargeResponse[]>(`${this.base}/user/${userId}`);
  }

  getById(id: number): Observable<RechargeResponse> {
    return this.http.get<RechargeResponse>(`${this.base}/${id}`);
  }

  downloadReceipt(data: any): Observable<Blob> {
    // This calls the notification-service via the Gateway
    const url = `${environment.apiUrl}/api/notifications/generate-receipt`;
    return this.http.post(url, data, { responseType: 'blob' });
  }
}
