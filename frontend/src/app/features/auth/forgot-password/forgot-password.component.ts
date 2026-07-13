import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LucideAngularModule, Mail, ArrowRight, ArrowLeft, Key, ShieldCheck } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, LucideAngularModule],
  template: `
    <div class="auth-container">
      <div class="auth-card glass">
        <div class="auth-logo">
          <lucide-icon name="zap" [size]="40" class="gold-icon"></lucide-icon>
          <h1>RechargeHub</h1>
        </div>

        <div class="auth-header">
          <h2>{{ step() === 1 ? 'Forgot Password?' : 'Reset Password' }}</h2>
          <p>{{ step() === 1 ? 'Enter your email to receive a verification code.' : 'Enter the code and your new password.' }}</p>
        </div>

        <!-- STEP 1: REQUEST OTP -->
        <form *ngIf="step() === 1" [formGroup]="forgotForm" (ngSubmit)="onRequestOtp()">
          <div class="form-group">
            <div class="input-container" [class.invalid]="isFieldInvalid(forgotForm, 'email')">
              <lucide-icon name="mail" [size]="18" class="field-icon"></lucide-icon>
              <input type="email" formControlName="email" placeholder="Email Address" />
            </div>
            <span class="err-hint" *ngIf="isFieldInvalid(forgotForm, 'email')">Valid email required</span>
          </div>

          <button type="submit" class="primary-btn full-width-btn" [disabled]="loading()">
            <span *ngIf="!loading()">Send Code</span>
            <lucide-icon *ngIf="!loading()" name="arrow-right" [size]="18"></lucide-icon>
            <lucide-icon *ngIf="loading()" name="loader-2" [size]="18" class="spin"></lucide-icon>
          </button>
        </form>

        <!-- STEP 2: RESET PASSWORD -->
        <form *ngIf="step() === 2" [formGroup]="resetForm" (ngSubmit)="onResetPassword()">
          <div class="form-group">
            <div class="input-container" [class.invalid]="isFieldInvalid(resetForm, 'otp')">
              <lucide-icon name="shield-check" [size]="18" class="field-icon"></lucide-icon>
              <input type="text" formControlName="otp" placeholder="6-Digit Code" maxlength="6" />
            </div>
          </div>

          <div class="form-group">
            <div class="input-container" [class.invalid]="isFieldInvalid(resetForm, 'newPassword')">
              <lucide-icon name="key" [size]="18" class="field-icon"></lucide-icon>
              <input type="password" formControlName="newPassword" placeholder="New Password (min 6 chars)" />
            </div>
          </div>

          <button type="submit" class="primary-btn full-width-btn" [disabled]="loading()">
            <span *ngIf="!loading()">Reset Password</span>
            <lucide-icon *ngIf="!loading()" name="arrow-right" [size]="18"></lucide-icon>
            <lucide-icon *ngIf="loading()" name="loader-2" [size]="18" class="spin"></lucide-icon>
          </button>
        </form>

        <div class="auth-footer-links">
          <a routerLink="/auth/login">
            <lucide-icon name="arrow-left" [size]="14"></lucide-icon>
            Back to Login
          </a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .auth-container { 
      min-height: 100vh; display: flex; align-items: center; justify-content: center;
      background: var(--bg-dark); padding: 20px;
    }
    .auth-card {
      width: 100%; max-width: 440px; padding: 48px; border-radius: 32px;
      border: 1px solid var(--glass-border);
    }
    .auth-logo { text-align: center; margin-bottom: 40px; }
    .auth-logo h1 { font-size: 28px; font-weight: 800; color: #fff; margin-top: 12px; }
    
    .auth-header { text-align: center; margin-bottom: 32px; }
    .auth-header h2 { font-size: 24px; font-weight: 700; color: #fff; margin-bottom: 12px; }
    .auth-header p { font-size: 15px; color: var(--text-secondary); }
    
    .input-container {
      display: flex; align-items: center; gap: 12px;
      background: rgba(255, 255, 255, 0.03); border: 1px solid var(--glass-border);
      border-radius: 16px; padding: 0 16px; transition: all 0.3s;
    }
    .input-container:focus-within { border-color: var(--accent-gold); background: rgba(255, 255, 255, 0.06); }
    .input-container.invalid { border-color: var(--accent-red); }
    
    .input-container input { 
      flex: 1; padding: 16px 0; background: none; border: none; outline: none; 
      color: #fff; font-size: 15px;
    }
    .field-icon { color: var(--text-muted); opacity: 0.5; }
    
    .full-width-btn { width: 100%; margin-top: 24px; height: 56px; }
    .auth-footer-links { text-align: center; margin-top: 32px; }
    .auth-footer-links a { 
      color: var(--accent-gold); font-weight: 600; text-decoration: none; 
      display: inline-flex; align-items: center; gap: 8px; transition: all 0.3s;
    }
    .auth-footer-links a:hover { transform: translateX(-5px); }

    .err-hint { font-size: 11px; color: var(--accent-red); margin-top: 6px; display: block; }
    .spin { animation: spin 1s linear infinite; }
    @keyframes spin { 100% { transform: rotate(360deg); } }
  `]
})
export class ForgotPasswordComponent {
  private fb     = inject(FormBuilder);
  private auth   = inject(AuthService);
  private toast  = inject(ToastService);
  private router = inject(Router);

  step = signal(1);
  loading = signal(false);

  forgotForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  resetForm = this.fb.group({
    otp:         ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]],
    newPassword: ['', [Validators.required, Validators.minLength(6)]]
  });

  isFieldInvalid(form: any, field: string): boolean {
    const control = form.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }

  onRequestOtp(): void {
    if (this.forgotForm.invalid) return;
    this.loading.set(true);
    const email = this.forgotForm.value.email!;

    this.auth.forgotPassword(email).subscribe({
      next: () => {
        this.loading.set(false);
        this.step.set(2);
        this.toast.success('Verification code sent to your email.');
      },
      error: (e) => {
        this.loading.set(false);
        this.toast.error(e?.error?.message || 'Failed to send reset code.');
      }
    });
  }

  onResetPassword(): void {
    if (this.resetForm.invalid) return;
    this.loading.set(true);
    const email = this.forgotForm.value.email!;
    const { otp, newPassword } = this.resetForm.value;

    this.auth.resetPassword(email, otp!, newPassword!).subscribe({
      next: () => {
        this.loading.set(false);
        this.toast.success('Password reset successful. Please login.');
        this.router.navigate(['/auth/login']);
      },
      error: (e: any) => {
        this.loading.set(false);
        this.toast.error(e.error?.message || 'Invalid OTP');
      }
    });
  }

  resendOtp(): void {
    const email = this.forgotForm.value.email;
    if (!email) return;

    this.loading.set(true);
    this.auth.resendOtp(email).subscribe({
      next: () => {
        this.loading.set(false);
        this.toast.success('New OTP sent to your email.');
      },
      error: (e) => {
        this.loading.set(false);
        this.toast.error(e?.error?.message || 'Failed to resend OTP.');
      }
    });
  }
}
