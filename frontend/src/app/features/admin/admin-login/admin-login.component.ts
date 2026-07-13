import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { LucideAngularModule, ShieldAlert, Key, Mail, Loader2, ArrowRight } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-admin-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LucideAngularModule],
  template: `
    <div class="admin-auth-container">
      <!-- Animated Background Elements -->
      <div class="bg-orb orb-1"></div>
      <div class="bg-orb orb-2"></div>
      
      <div class="auth-card">
        <div class="brand-section">
          <div class="shield-icon">
            <lucide-icon name="shield-alert" [size]="40"></lucide-icon>
          </div>
          <h1>Admin Portal</h1>
          <p>Restricted Access Area</p>
        </div>

        <form [formGroup]="loginForm" (ngSubmit)="onSubmit()" class="auth-form">
          <div class="form-group">
            <label>Administrator Email</label>
            <div class="input-wrapper" [class.error]="isFieldInvalid('email')">
              <lucide-icon name="mail" [size]="18" class="input-icon"></lucide-icon>
              <input type="email" formControlName="email" placeholder="admin@rechargehub.com" />
            </div>
            <span class="error-msg" *ngIf="isFieldInvalid('email')">
              <span *ngIf="loginForm.get('email')?.errors?.['required']">Authorized email required</span>
              <span *ngIf="loginForm.get('email')?.errors?.['email']">Invalid email format</span>
            </span>
          </div>

          <div class="form-group">
            <label>Master Password</label>
            <div class="input-wrapper" [class.error]="isFieldInvalid('password')">
              <lucide-icon name="key" [size]="18" class="input-icon"></lucide-icon>
              <input type="password" formControlName="password" placeholder="••••••••" />
            </div>
            <span class="error-msg" *ngIf="isFieldInvalid('password')">Master password required</span>
          </div>

          <button type="submit" class="btn btn-primary btn-block" [disabled]="loginForm.invalid || loading()">
            <span *ngIf="!loading()">Authenticate <lucide-icon name="arrow-right" [size]="18"></lucide-icon></span>
            <lucide-icon *ngIf="loading()" name="loader-2" [size]="18" class="spin"></lucide-icon>
          </button>
        </form>
        
        <div class="back-link">
          <a routerLink="/auth/login" class="back-btn">
            <lucide-icon name="arrow-left" [size]="16"></lucide-icon>
            Back to User Portal
          </a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .admin-auth-container {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #03010A;
      position: relative;
      overflow: hidden;
      font-family: 'Inter', sans-serif;
    }
    
    .bg-orb { position: absolute; border-radius: 50%; filter: blur(100px); z-index: 0; opacity: 0.3; }
    .orb-1 { width: 400px; height: 400px; background: #e1ca96; top: -100px; left: -100px; }
    .orb-2 { width: 500px; height: 500px; background: #2dd4bf; bottom: -200px; right: -100px; opacity: 0.1; }

    .auth-card {
      width: 100%; max-width: 420px; background: rgba(24, 26, 28, 0.7); backdrop-filter: blur(20px);
      border: 1px solid rgba(225, 202, 150, 0.2); border-radius: 24px; padding: 48px; position: relative;
      z-index: 1; box-shadow: 0 24px 48px rgba(0,0,0,0.4);
    }

    .brand-section {
      text-align: center; margin-bottom: 40px;
      .shield-icon { width: 80px; height: 80px; margin: 0 auto 24px; background: rgba(225, 202, 150, 0.1); color: var(--accent-gold); border-radius: 24px; display: flex; align-items: center; justify-content: center; border: 1px solid rgba(225, 202, 150, 0.2); }
      h1 { color: #f8fafc; font-size: 28px; font-weight: 800; margin: 0 0 8px; letter-spacing: -0.5px; }
      p { color: #ef4444; font-size: 14px; font-weight: 600; text-transform: uppercase; letter-spacing: 2px; margin: 0; }
    }

    .form-group { margin-bottom: 24px; }
    label { display: block; color: #94a3b8; font-size: 13px; font-weight: 600; margin-bottom: 8px; text-transform: uppercase; letter-spacing: 0.5px; }
    
    .input-wrapper {
      position: relative; display: flex; align-items: center;
      background: rgba(0,0,0,0.2); border: 1px solid rgba(255,255,255,0.1); border-radius: 12px; transition: all 0.3s ease;
      &:focus-within { border-color: var(--accent-gold); background: rgba(0,0,0,0.4); box-shadow: 0 0 0 3px rgba(225,202,150,0.1); }
      &.error { border-color: #ef4444; }
      
      .input-icon { position: absolute; left: 16px; color: #64748b; }
      input { width: 100%; background: none; border: none; padding: 16px 16px 16px 48px; color: white; font-size: 15px; outline: none; }
    }
    
    .error-msg { display: block; color: #ef4444; font-size: 12px; margin-top: 8px; font-weight: 500; }
    
    .btn-block { width: 100%; display: flex; justify-content: center; align-items: center; gap: 8px; padding: 16px; font-size: 16px; font-weight: 600; border-radius: 12px; }
    
    .back-link { text-align: center; margin-top: 32px; a { color: #64748b; font-size: 14px; font-weight: 500; text-decoration: none; transition: color 0.2s; &:hover { color: white; } } }
    
    .spin { animation: spin 1s linear infinite; }
    @keyframes spin { 100% { transform: rotate(360deg); } }
  `]
})
export class AdminLoginComponent {
  private fb = inject(FormBuilder);
  private authSvc = inject(AuthService);
  private toast = inject(ToastService);
  private router = inject(Router);

  loading = signal(false);

  loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  isFieldInvalid(field: string): boolean {
    const control = this.loginForm.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }

  onSubmit() {
    if (this.loginForm.invalid) return;

    this.loading.set(true);
    const val = this.loginForm.value;
    
    this.authSvc.adminLogin({ email: val.email!, password: val.password! }).subscribe({
      next: (res) => {
        this.loading.set(false);
        console.log('Admin login response:', res);
        if (res.token) {
          // Verify role
          const isAdmin = this.authSvc.isAdmin();
          console.log('Is user admin?', isAdmin);
          if (isAdmin) {
            this.toast.success('Admin login successful');
            this.router.navigate(['/admin/dashboard']);
          } else {
            this.toast.error('Access Denied. Not an administrator.');
            this.authSvc.logout();
          }
        }
      },
      error: (err) => {
        this.loading.set(false);
        console.error('Admin login error:', err);
        this.toast.error(err.error?.error || 'Invalid credentials');
      }
    });
  }

}
