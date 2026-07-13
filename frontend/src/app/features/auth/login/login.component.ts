import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { LucideAngularModule, Zap, Mail, Lock, Eye, EyeOff, Shield } from 'lucide-angular';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, LucideAngularModule],
  template: `
    <div class="auth-page">
      <!-- Background Decorative Elements -->
      <div class="bg-glow-1"></div>
      <div class="bg-glow-2"></div>

      <div class="auth-card glass">
        <div class="auth-logo">
          <div class="logo-box">
            <lucide-icon name="zap" [size]="32" class="gold-icon"></lucide-icon>
          </div>
          <h1>Recharge<span>Hub</span></h1>
        </div>

        <div class="auth-header">
          <h2>Sophisticated Access</h2>
          <p>Please authenticate to enter your sanctuary.</p>
        </div>

        <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
          <div class="field">
            <label>Email Address</label>
            <div class="input-container" [class.invalid]="loginForm.get('email')?.touched && loginForm.get('email')?.invalid">
              <lucide-icon name="mail" [size]="18" class="field-icon"></lucide-icon>
              <input type="email" formControlName="email" placeholder="Email address"/>
            </div>
            <span class="err-hint" *ngIf="loginForm.get('email')?.touched && loginForm.get('email')?.invalid">
              <span *ngIf="loginForm.get('email')?.errors?.['required']">* Email address is required</span>
              <span *ngIf="loginForm.get('email')?.errors?.['email']">* Please enter a valid email format</span>
            </span>
          </div>

          <div class="field">
            <label>Passcode</label>
            <div class="input-container" [class.invalid]="loginForm.get('password')?.touched && loginForm.get('password')?.invalid">
              <lucide-icon name="lock" [size]="18" class="field-icon"></lucide-icon>
              <input [type]="showPwd() ? 'text' : 'password'" formControlName="password" placeholder="••••••••"/>
              <button type="button" class="eye-toggle" (click)="showPwd.set(!showPwd())">
                <lucide-icon [name]="showPwd() ? 'eye-off' : 'eye'" [size]="18"></lucide-icon>
              </button>
            </div>
            <span class="err-hint" *ngIf="loginForm.get('password')?.touched && loginForm.get('password')?.invalid">
              * Passcode is mandatory
            </span>
          </div>

          <div class="auth-alert error" *ngIf="error()">
            <lucide-icon name="shield" [size]="16"></lucide-icon>
            {{ error() }}
          </div>

          <div class="forgot-link">
            <a routerLink="/auth/forgot-password">Forgot Password?</a>
          </div>

          <button type="submit" class="btn btn-primary full-width-btn" [disabled]="loading()">
            <span *ngIf="!loading()">Sign In Securely</span>
            <span *ngIf="loading()" class="btn-loader">Authenticating...</span>
          </button>
        </form>

        <div class="auth-footer-links">
          <p>New to RechargeHub? <a routerLink="/auth/register">Create Account</a></p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .auth-page { 
      min-height: 100vh; display: flex; align-items: center; justify-content: center; 
      padding: 24px; background: #03010A; position: relative; overflow: hidden;
    }

    .bg-glow-1 {
      position: absolute; top: -100px; right: -100px; width: 400px; height: 400px;
      background: radial-gradient(circle, rgba(225, 202, 150, 0.05) 0%, transparent 70%);
      filter: blur(60px);
    }

    .bg-glow-2 {
      position: absolute; bottom: -100px; left: -100px; width: 400px; height: 400px;
      background: radial-gradient(circle, rgba(16, 185, 129, 0.03) 0%, transparent 70%);
      filter: blur(60px);
    }

    .auth-card { 
      width: 100%; max-width: 460px; padding: 48px; border-radius: 32px; 
      position: relative; z-index: 10; animation: slideIn 0.6s cubic-bezier(0.2, 0.8, 0.2, 1);
    }
    
    @keyframes slideIn {
      from { opacity: 0; transform: translateY(40px) scale(0.95); }
      to   { opacity: 1; transform: translateY(0) scale(1); }
    }

    .auth-logo { display: flex; flex-direction: column; align-items: center; gap: 16px; margin-bottom: 40px; }
    .logo-box {
      width: 64px; height: 64px; background: rgba(225, 202, 150, 0.1);
      border: 1px solid rgba(255,255,255,0.1); border-radius: 20px;
      display: flex; align-items: center; justify-content: center;
      box-shadow: 0 0 20px rgba(225, 202, 150, 0.1);
    }
    .gold-icon { color: #E1CA96; }
    .auth-logo h1 { font-size: 28px; font-weight: 800; color: white; margin: 0; letter-spacing: -1px; }
    .auth-logo h1 span { color: #E1CA96; }
    
    .auth-header { text-align: center; margin-bottom: 40px; }
    .auth-header h2 { font-size: 24px; font-weight: 700; color: white; margin-bottom: 12px; letter-spacing: -0.5px; }
    .auth-header p { font-size: 15px; color: #94A3B8; line-height: 1.6; }
    
    .input-container {
      display: flex; align-items: center; gap: 14px;
      background: rgba(255, 255, 255, 0.03); border: 1px solid rgba(255,255,255,0.08);
      border-radius: 16px; padding: 0 18px; transition: all 0.3s;
      height: 60px; /* Fixed height for better alignment */
    }
    .input-container:focus-within { 
      border-color: #E1CA96; 
      background: rgba(255, 255, 255, 0.06);
      box-shadow: 0 0 0 4px rgba(225, 202, 150, 0.1); 
    }
    .input-container.invalid { border-color: #ef4444; background: rgba(244, 63, 94, 0.05); }
    .field-icon { color: #64748B; flex-shrink: 0; }
    
    .input-container input { 
      flex: 1; background: transparent !important; border: none; outline: none; 
      color: white !important; font-size: 15px; font-weight: 600;
      height: 100%; width: 100%;
    }

    /* Fix for Browser Autofill White Background */
    input:-webkit-autofill,
    input:-webkit-autofill:hover, 
    input:-webkit-autofill:focus, 
    input:-webkit-autofill:active {
      -webkit-box-shadow: 0 0 0 30px #121416 inset !important;
      -webkit-text-fill-color: white !important;
      transition: background-color 5000s ease-in-out 0s;
    }
    
    .eye-toggle { background: none; border: none; color: #64748B; cursor: pointer; padding: 8px; display: flex; align-items: center; justify-content: center; }
    .eye-toggle:hover { color: #E1CA96; }

    .err-hint { font-size: 11px; color: #ef4444; margin-top: 6px; display: block; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; }
    .full-width-btn { width: 100%; margin-top: 12px; height: 56px; }
    
    .auth-footer-links { text-align: center; margin-top: 32px; font-size: 14px; color: #94A3B8; }
    .auth-footer-links a { color: #E1CA96; font-weight: 700; text-decoration: none; cursor: pointer; transition: all 0.3s; }
    .auth-footer-links a:hover { letter-spacing: 0.5px; }
    
    .auth-alert { 
      padding: 14px 20px; border-radius: 14px; font-size: 14px; margin-bottom: 24px; 
      display: flex; align-items: center; gap: 10px; font-weight: 600;
    }
    .auth-alert.error { background: rgba(244, 63, 94, 0.1); color: #ef4444; border: 1px solid rgba(244, 63, 94, 0.2); }
    
    .forgot-link { text-align: right; margin-top: -12px; margin-bottom: 24px; }
    .forgot-link a { color: #E1CA96; font-size: 13px; font-weight: 600; text-decoration: none; transition: all 0.3s; opacity: 0.8; }
    .forgot-link a:hover { opacity: 1; text-decoration: underline; }

    .otp-stage { text-align: center; }
    .otp-input-group { margin-bottom: 40px; position: relative; }
    .otp-master-input { 
      text-align: center; font-size: 44px !important; letter-spacing: 16px; font-weight: 800; 
      padding: 16px !important; background: none; border: none; color: white; 
      outline: none; width: 100%; font-family: monospace;
    }
    .otp-underline { height: 2px; background: linear-gradient(90deg, transparent, #E1CA96, transparent); width: 100%; border-radius: 2px; }
  `]
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  loading = signal(false);
  error = signal('');
  showPwd = signal(false);

  loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set('');

    this.auth.login(this.loginForm.value as any).subscribe({
      next: (res: any) => {
        this.loading.set(false);
        this.toast.success('Login successful!');
        this.router.navigate(['/dashboard']);
      },
      error: (e: any) => {
        this.loading.set(false);
        this.error.set(e.error?.message || 'Login failed');
      }
    });
  }

}
