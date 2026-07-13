import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { LucideAngularModule, Zap, Mail, Lock, User, Phone } from 'lucide-angular';

@Component({
  selector: 'app-register',
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
          <h2>{{ step() === 1 ? 'Join the Symphony' : 'Verify Identity' }}</h2>
          <p>{{ step() === 1 ? 'Register now to access elite recharging features.' : 'An elite verification code was sent to ' + form.value.email }}</p>
        </div>

        <form [formGroup]="form" (ngSubmit)="submit()" *ngIf="step() === 1">
          <div class="field">
            <label>Full Name</label>
            <div class="input-container" [class.invalid]="form.get('name')?.touched && form.get('name')?.invalid">
              <lucide-icon name="user" [size]="18" class="field-icon"></lucide-icon>
              <input type="text" formControlName="name" placeholder="E.g. Johnathan Doe"/>
            </div>
            <span class="err-hint" *ngIf="form.get('name')?.touched && form.get('name')?.invalid">
              <span *ngIf="form.get('name')?.errors?.['required']">* Full name is required</span>
              <span *ngIf="form.get('name')?.errors?.['pattern']">* Only letters and spaces allowed</span>
              <span *ngIf="form.get('name')?.errors?.['minlength']">* Name is too short (min 2 chars)</span>
            </span>
          </div>

          <div class="field">
            <label>Electronic Mail</label>
            <div class="input-container" [class.invalid]="form.get('email')?.touched && form.get('email')?.invalid">
              <lucide-icon name="mail" [size]="18" class="field-icon"></lucide-icon>
              <input type="email" formControlName="email" placeholder="you@domain.com"/>
            </div>
            <span class="err-hint" *ngIf="form.get('email')?.touched && form.get('email')?.invalid">
              <span *ngIf="form.get('email')?.errors?.['required']">* Email address is required</span>
              <span *ngIf="form.get('email')?.errors?.['email']">* Please enter a valid email format</span>
            </span>
          </div>

          <div class="field">
            <label>Mobile Number</label>
            <div class="input-container" [class.invalid]="form.get('phoneNumber')?.touched && form.get('phoneNumber')?.invalid">
              <lucide-icon name="phone" [size]="18" class="field-icon"></lucide-icon>
              <input type="tel" formControlName="phoneNumber" placeholder="9876543210" maxlength="10"/>
            </div>
            <span class="err-hint" *ngIf="form.get('phoneNumber')?.touched && form.get('phoneNumber')?.invalid">
              <span *ngIf="form.get('phoneNumber')?.errors?.['required']">* Mobile number is required</span>
              <span *ngIf="form.get('phoneNumber')?.errors?.['pattern']">* Exactly 10 digits starting with 6-9</span>
            </span>
          </div>

          <div class="field">
            <label>Secure Passcode</label>
            <div class="input-container" [class.invalid]="form.get('password')?.touched && form.get('password')?.invalid">
              <lucide-icon name="lock" [size]="18" class="field-icon"></lucide-icon>
              <input type="password" formControlName="password" placeholder="Min 6 characters"/>
            </div>
            <span class="err-hint" *ngIf="form.get('password')?.touched && form.get('password')?.invalid">
              <span *ngIf="form.get('password')?.errors?.['required']">* Password is required</span>
              <span *ngIf="form.get('password')?.errors?.['minlength']">* Password must be at least 6 characters</span>
            </span>
          </div>

          <div class="auth-alert error" *ngIf="error()">{{ error() }}</div>
          <div class="auth-alert success" *ngIf="success()">{{ success() }}</div>

          <button type="submit" class="btn btn-primary full-width-btn" [disabled]="loading()">
            {{ loading() ? 'Processing...' : 'Create Account' }}
          </button>
        </form>

        <form [formGroup]="otpForm" (ngSubmit)="onVerify()" *ngIf="step() === 2" class="otp-stage">
          <div class="otp-input-group">
            <input type="text" formControlName="otp" placeholder="••••••" maxlength="6" class="otp-master-input" />
            <div class="otp-underline"></div>
          </div>
          <div class="auth-alert error" *ngIf="error()">{{ error() }}</div>
          
          <button type="submit" class="btn btn-success full-width-btn" [disabled]="loading() || otpForm.invalid">
            {{ loading() ? 'Verifying...' : 'Confirm Identity' }}
          </button>
          
          <div class="auth-footer-links">
            <p>Code not received? <a (click)="resendOtp()">Resend OTP</a></p>
          </div>
        </form>

        <div class="auth-footer-links" *ngIf="step() === 1">
          <p>Already a member? <a routerLink="/auth/login">Sign in</a></p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .auth-page { 
      min-height: 100vh; display: flex; align-items: center; justify-content: center; 
      padding: 24px; background: var(--bg-main); position: relative; overflow: hidden;
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
      width: 100%; max-width: 480px; padding: 48px; border-radius: 32px; 
      position: relative; z-index: 10; animation: slideIn 0.6s cubic-bezier(0.2, 0.8, 0.2, 1);
    }
    
    @keyframes slideIn {
      from { opacity: 0; transform: translateY(40px) scale(0.95); }
      to   { opacity: 1; transform: translateY(0) scale(1); }
    }

    .auth-logo { display: flex; flex-direction: column; align-items: center; gap: 16px; margin-bottom: 32px; }
    .logo-box {
      width: 60px; height: 60px; background: rgba(225, 202, 150, 0.1);
      border: 1px solid var(--glass-border); border-radius: 18px;
      display: flex; align-items: center; justify-content: center;
      box-shadow: var(--glow-gold);
    }
    .gold-icon { color: var(--accent-gold); }
    .auth-logo h1 { font-size: 26px; font-weight: 800; color: var(--text-primary); margin: 0; letter-spacing: -1px; }
    .auth-logo h1 span { color: var(--accent-gold); }
    
    .auth-header { text-align: center; margin-bottom: 32px; }
    .auth-header h2 { font-size: 24px; font-weight: 700; color: var(--text-primary); margin-bottom: 12px; }
    .auth-header p { font-size: 15px; color: var(--text-secondary); }
    
    .input-container {
      display: flex; align-items: center; gap: 12px;
      background: rgba(255, 255, 255, 0.03); border: 1px solid var(--glass-border);
      border-radius: 16px; padding: 0 16px; transition: all 0.3s;
      backdrop-filter: var(--glass-blur);
    }
    .input-container:focus-within { 
      border-color: var(--accent-gold); 
      background: rgba(255, 255, 255, 0.06);
      box-shadow: 0 0 0 4px rgba(225, 202, 150, 0.1); 
    }
    .input-container.invalid { border-color: var(--accent-red); background: rgba(244, 63, 94, 0.05); }
    .input-container.invalid:focus-within { box-shadow: 0 0 0 4px rgba(244, 63, 94, 0.1); }
    .field-icon { color: var(--text-muted); opacity: 0.5; }
    .input-container input { 
      flex: 1; padding: 16px 0; background: none; border: none; outline: none; 
      color: var(--text-primary); font-size: 15px; font-weight: 500;
    }
    
    .full-width-btn { width: 100%; margin-top: 24px; height: 56px; }
    
    .auth-footer-links { text-align: center; margin-top: 32px; font-size: 14px; color: var(--text-secondary); }
    .auth-footer-links a { color: var(--accent-gold); font-weight: 700; text-decoration: none; cursor: pointer; transition: all 0.3s; }
    .auth-footer-links a:hover { letter-spacing: 0.5px; }
    
    .err-hint { font-size: 11px; color: var(--accent-red); margin-top: 6px; display: block; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; }
    
    .auth-alert { 
      padding: 14px 20px; border-radius: 14px; font-size: 14px; margin-top: 24px; 
      display: flex; align-items: center; gap: 10px; font-weight: 600;
    }
    .auth-alert.error { background: rgba(244, 63, 94, 0.1); color: var(--accent-red); border: 1px solid rgba(244, 63, 94, 0.2); }
    .auth-alert.success { background: rgba(16, 185, 129, 0.1); color: var(--accent-emerald); border: 1px solid rgba(16, 185, 129, 0.2); }

    .otp-stage { text-align: center; }
    .otp-input-group { margin-bottom: 40px; position: relative; }
    .otp-master-input { 
      text-align: center; font-size: 44px !important; letter-spacing: 16px; font-weight: 800; 
      padding: 16px !important; background: none; border: none; color: var(--text-primary); 
      outline: none; width: 100%; font-family: monospace;
    }
    .otp-underline { height: 2px; background: var(--gradient-gold); width: 100%; border-radius: 2px; box-shadow: var(--glow-gold); }
  `]
})
export class RegisterComponent {
  private fb     = inject(FormBuilder);
  private auth   = inject(AuthService);
  private router = inject(Router);
  private toast  = inject(ToastService);

  step = signal(1); // 1 = Details, 2 = OTP
  loading = signal(false);
  error   = signal('');
  success = signal('');

  form = this.fb.group({
    name:        ['', [Validators.required, Validators.pattern(/^[a-zA-Z ]+$/), Validators.minLength(2), Validators.maxLength(50)]],
    email:       ['', [Validators.required, Validators.email]],
    phoneNumber: ['', [Validators.required, Validators.pattern(/^[6-9]\d{9}$/)]],
    password:    ['', [Validators.required, Validators.minLength(6)]]
  });

  otpForm = this.fb.group({
    otp: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]]
  });

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    if (this.loading()) return;
    this.loading.set(true);
    this.error.set('');
    
    this.auth.register(this.form.value as any).subscribe({
      next: () => {
        this.loading.set(false);
        this.step.set(2);
        this.toast.info('OTP sent to your email.');
      },
      error: (e) => {
        const msg = e?.error?.message || 'Registration failed. Try again.';
        this.error.set(msg);
        this.toast.error(msg);
        this.loading.set(false);
      }
    });
  }

  onVerify(): void {
    if (this.otpForm.invalid) return;
    this.loading.set(true);
    this.error.set('');

    this.auth.verifyAccount(this.form.value.email!, this.otpForm.value.otp!).subscribe({
      next: () => {
        this.loading.set(false);
        this.success.set('Account verified! Redirecting to login...');
        this.toast.success('Account verified successfully!');
        setTimeout(() => this.router.navigate(['/auth/login']), 1500);
      },
      error: (e: any) => {
        this.loading.set(false);
        this.error.set(e.error?.message || 'Invalid OTP');
      }
    });
  }

  resendOtp(): void {
    const email = this.form.value.email;
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
