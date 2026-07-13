import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {

  private base = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) {}

  getById(id: number): Observable<User> {
    return this.http.get<User>(`${this.base}/${id}`);
  }

  // ADMIN
  getAll(): Observable<User[]> {
    return this.http.get<User[]>(`${this.base}/admin/all`);
  }

  deleteUser(id: number): Observable<any> {
    return this.http.delete(`${this.base}/admin/${id}`);
  }

  registerAdmin(req: any): Observable<User> {
    return this.http.post<User>(`${this.base}/admin/register`, req);
  }
}
