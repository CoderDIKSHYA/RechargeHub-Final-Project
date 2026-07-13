import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Operator, Plan } from '../models/operator.model';

@Injectable({ providedIn: 'root' })
export class OperatorService {

  private base = `${environment.apiUrl}/operators`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Operator[]> {
    return this.http.get<Operator[]>(this.base);
  }

  getById(id: number): Observable<Operator> {
    return this.http.get<Operator>(`${this.base}/${id}`);
  }

  getPlanById(id: number): Observable<Plan> {
    return this.http.get<Plan>(`${this.base}/plans/${id}`);
  }

  // ADMIN
  create(op: Partial<Operator>): Observable<Operator> {
    return this.http.post<Operator>(this.base, op);
  }

  update(id: number, op: Partial<Operator>): Observable<Operator> {
    return this.http.put<Operator>(`${this.base}/${id}`, op);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  createPlan(operatorId: number, plan: Partial<Plan>): Observable<Plan> {
    return this.http.post<Plan>(`${this.base}/${operatorId}/plans`, plan);
  }

  updatePlan(id: number, plan: Partial<Plan>): Observable<Plan> {
    return this.http.put<Plan>(`${this.base}/plans/${id}`, plan);
  }

  deletePlan(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/plans/${id}`);
  }
}
