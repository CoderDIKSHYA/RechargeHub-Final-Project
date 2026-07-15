import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest, User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly TOKEN_KEY = 'rh_token';
  private readonly USER_KEY  = 'rh_user';

  currentUser = signal<User | null>(this.loadUser());

  constructor(private http: HttpClient, private router: Router) {}

  login(req: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/users/login`, req).pipe(
      tap(res => {
        if (res.token !== 'OTP_SENT') {
          this.saveSession(res);
        }ng 
      })
    );
  }

  adminLogin(req: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/admin/login`, req).pipe(
      tap(res => this.saveSession(res))
    );
  }

  verifyLogin(email: string, otp: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/users/verify-login`, { email, otp }).pipe(
      tap(res => this.saveSession(res))
    );
  }

  register(req: RegisterRequest): Observable<User> {
    return this.http.post<User>(`${environment.apiUrl}/users/register`, req);
  }

  verifyAccount(email: string, otp: string): Observable<{message: string}> {
    return this.http.post<{message: string}>(`${environment.apiUrl}/users/verify-account`, { email, otp });
  }

  resendOtp(email: string): Observable<{message: string}> {
    return this.http.post<{message: string}>(`${environment.apiUrl}/users/resend-otp`, { email });
  }

  forgotPassword(email: string): Observable<{message: string}> {
    return this.http.post<{message: string}>(`${environment.apiUrl}/users/forgot-password`, { email });
  }

  resetPassword(email: string, otp: string, newPassword: string): Observable<{message: string}> {
    return this.http.post<{message: string}>(`${environment.apiUrl}/users/reset-password`, { email, otp, newPassword });
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUser.set(null);
    this.router.navigate(['/auth/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  isAdmin(): boolean {
    return this.currentUser()?.role === 'ROLE_ADMIN';
  }

  private saveSession(res: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, res.token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(res.user));
    this.currentUser.set(res.user);
  }

  private loadUser(): User | null {
    const raw = localStorage.getItem(this.USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }
}
