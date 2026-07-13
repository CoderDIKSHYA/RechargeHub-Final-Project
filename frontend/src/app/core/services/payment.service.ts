import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private http = inject(HttpClient);
  // Correctly route through Gateway (8989) with /api prefix
  private apiUrl = 'http://localhost:8989/api/payments/razorpay'; 

  createOrder(amount: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/create-order`, { amount });
  }

  verifyPayment(paymentData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/verify-signature`, paymentData);
  }
}
