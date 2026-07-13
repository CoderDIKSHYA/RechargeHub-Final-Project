import { Component, OnInit, signal, inject, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { OperatorService } from '../../core/services/operator.service';
import { RechargeService } from '../../core/services/recharge.service';
import { ToastService } from '../../core/services/toast.service';
import { PaymentService } from '../../core/services/payment.service';
import { Operator, Plan } from '../../core/models/operator.model';
import { environment } from '../../../environments/environment';
import { LucideAngularModule, ChevronLeft, CheckCircle, Smartphone, Search, CreditCard, Wallet, Landmark, User, History, Users } from 'lucide-angular';

declare var Razorpay: any;

@Component({
  selector: 'app-recharge',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LucideAngularModule],
  template: `
    <div class="recharge-page">
      
      <!-- Stepper Header -->
      <div class="recharge-stepper" *ngIf="step() < 5">
        <div class="step-indicator" [class.active]="step() === 1" [class.done]="step() > 1">1</div>
        <div class="step-line"></div>
        <div class="step-indicator" [class.active]="step() === 3" [class.done]="step() > 3">2</div>
        <div class="step-line"></div>
        <div class="step-indicator" [class.active]="step() === 4" [class.done]="step() > 4">3</div>
      </div>

      <!-- Step 1: Mobile & Operator Selection -->
      <div class="recharge-content" *ngIf="step() <= 2">
        <div class="content-header">
          <h1>Recharge<span>Hub</span></h1>
          <p>Instant activation for your premium connection.</p>
        </div>

        <div class="glass-card main-input-card">
          <form [formGroup]="mobileForm">
            <div class="premium-input-group" 
                 [class.detected]="detectedOperator()"
                 [class.is-invalid]="mobileForm.get('mobile')?.invalid && (mobileForm.get('mobile')?.touched || mobileForm.get('mobile')?.dirty)"
                 [class.is-valid]="mobileForm.get('mobile')?.valid && mobileForm.get('mobile')?.value?.length === 10">
              
              <div class="input-icon-left">
                <lucide-icon name="smartphone" [size]="24" class="gold-icon"></lucide-icon>
              </div>

              <input type="tel" formControlName="mobile" placeholder="Enter Mobile Number" maxlength="10" (input)="onMobileInput()"/>
              
              <div class="input-status-tray">
                <lucide-icon name="check-circle" [size]="22" class="text-emerald bounce-in" *ngIf="mobileForm.get('mobile')?.valid && mobileForm.get('mobile')?.value?.length === 10"></lucide-icon>
                <lucide-icon name="shield-alert" [size]="22" class="text-rose shake" *ngIf="mobileForm.get('mobile')?.invalid && (mobileForm.get('mobile')?.touched || mobileForm.get('mobile')?.dirty)"></lucide-icon>
              </div>

              <button class="contact-trigger" (click)="openContacts()" type="button">
                <lucide-icon name="users" [size]="20"></lucide-icon>
              </button>
            </div>

            <!-- Visible Validation Message -->
            <div class="validation-feedback fade-in" *ngIf="mobileForm.get('mobile')?.invalid && (mobileForm.get('mobile')?.touched || mobileForm.get('mobile')?.dirty)">
               <div class="error-pill">
                 <lucide-icon name="shield-alert" [size]="14"></lucide-icon>
                 <span>Number must be 10 digits starting with 6-9</span>
               </div>
            </div>

            <!-- Success Badge for Circle Detection -->
            <div class="detection-badge-premium fade-in-up" *ngIf="detectedOperator()">
              <div class="db-main">
                <div class="db-logo-container">
                  <img [src]="detectedOperator()?.logoUrl" class="db-logo" *ngIf="detectedOperator()?.logoUrl" />
                  <div class="db-logo-fallback" *ngIf="!detectedOperator()?.logoUrl">
                    {{ detectedOperator()?.name?.charAt(0) }}
                  </div>
                </div>
                <div class="db-details">
                  <span class="db-op-name">{{ detectedOperator()?.name }}</span>
                  <span class="db-circle">📍 {{ detectedCircle() }} Circle</span>
                </div>
              </div>
              <button class="db-change-btn" (click)="step.set(2)" type="button">Change</button>
            </div>
          </form>

          <!-- Skeleton Loader for Operators -->
          <div class="operator-grid" *ngIf="operators().length === 0">
            <div class="skeleton-op-card" *ngFor="let i of [1,2,3,4]">
              <div class="skeleton-circle shimmer"></div>
              <div class="skeleton-line shimmer"></div>
            </div>
          </div>

          <div class="operator-grid fade-in" *ngIf="step() === 2 && operators().length > 0">
            <div class="op-card" *ngFor="let op of operators()" 
                 [class.selected]="selectedOperator()?.id === op.id" 
                 (click)="selectOperator(op)">
              <div class="op-logo-wrap">
                <img [src]="op.logoUrl" alt="{{op.name}}" *ngIf="op.logoUrl"/>
                <span *ngIf="!op.logoUrl">{{ op.name.charAt(0) }}</span>
              </div>
              <span class="op-name">{{ op.name }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Step 3: Plans Grid with Skeleton -->
      <div class="plans-view fade-in-up" *ngIf="step() === 3">
        <div class="context-pill">
          <div class="cp-info">
            <span class="cp-mobile">{{ mobileForm.value.mobile }}</span>
            <span class="cp-operator">{{ selectedOperator()?.name }} • {{ detectedCircle() }}</span>
          </div>
          <button class="cp-edit" (click)="step.set(2)">Change</button>
        </div>

        <!-- Skeleton for Plans -->
        <div class="plans-grid" *ngIf="loading()">
          <div class="plan-card skeleton glass" *ngFor="let i of [1,2,3,4,5,6]">
            <div class="skeleton-line-lg shimmer"></div>
            <div class="skeleton-line-md shimmer"></div>
            <div class="skeleton-line-sm shimmer"></div>
          </div>
        </div>

        <div class="plans-grid" *ngIf="!loading()">
          <div class="plan-card glass hoverable" *ngFor="let plan of plans()" (click)="selectPlan(plan)">
            <div class="plan-badge" *ngIf="plan.amount > 500">Popular</div>
            <div class="plan-top">
              <span class="p-amount">₹{{ plan.amount }}</span>
              <span class="p-validity">{{ plan.validity }}</span>
            </div>
            <p class="p-desc">{{ plan.description }}</p>
            <div class="plan-footer">
              <button class="btn btn-primary btn-sm">Select Plan</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Step 4: Checkout -->
      <div class="checkout-view slide-up" *ngIf="step() === 4">
        <div class="checkout-header">
          <button class="back-link" (click)="step.set(3)">
            <lucide-icon name="chevron-left" [size]="18"></lucide-icon> Back to Plans
          </button>
          <h2>Complete Payment</h2>
        </div>

        <div class="summary-card glass">
          <div class="s-row">
            <span class="s-label">Recharging for</span>
            <span class="s-val">{{ mobileForm.value.mobile }}</span>
          </div>
          <div class="s-row">
            <span class="s-label">Plan details</span>
            <span class="s-val">{{ selectedPlan()?.validity }} | {{ selectedPlan()?.description }}</span>
          </div>
          <div class="s-divider"></div>
          <div class="s-row total">
            <span class="s-label">Total Amount</span>
            <span class="s-amount">₹{{ selectedPlan()?.amount }}</span>
          </div>
        </div>

        <div class="payment-section">
          <h3>RechargeHub <span>Secure Pay</span></h3>
          <div class="method-list">
            <div class="method-item" *ngFor="let m of paymentMethods"
                 [class.active]="paymentMethod() === m.value" (click)="paymentMethod.set(m.value)">
              <div class="m-icon">
                <lucide-icon [name]="m.iconName" [size]="20"></lucide-icon>
              </div>
              <span class="m-label">{{ m.label }}</span>
              <div class="m-check" *ngIf="paymentMethod() === m.value">
                <lucide-icon name="check-circle" [size]="16"></lucide-icon>
              </div>
            </div>
          </div>
        </div>

        <div class="pay-button-container">
          <button class="btn btn-success pay-btn" [disabled]="loading()" (click)="submit()">
            <span *ngIf="!loading()">Authorize Payment (₹{{ selectedPlan()?.amount }})</span>
            <span *ngIf="loading()">Processing Securely...</span>
          </button>
        </div>
      </div>

      <!-- Step 5: Success Confirmation -->
      <div class="success-view" *ngIf="step() === 5">
        <div class="success-box glass fade-in-up">
          <div class="success-glow"></div>
          <div class="success-icon-wrap">
            <lucide-icon name="check-circle" [size]="48" class="emerald-icon"></lucide-icon>
          </div>
          <h1>Transaction Successful</h1>
          <p>Your recharge of <strong>₹{{ selectedPlan()?.amount }}</strong> has been activated.</p>

          <div class="details-list">
            <div class="d-item"><span>Reference ID</span><strong>#RH-{{ lastTransactionId() }}</strong></div>
            <div class="d-item"><span>Mobile Number</span><strong>{{ mobileForm.value.mobile }}</strong></div>
            <div class="d-item"><span>Operator</span><strong>{{ selectedOperator()?.name }}</strong></div>
            <div class="d-item"><span>Date & Time</span><strong>{{ currentTime() }}</strong></div>
          </div>

          <div class="success-footer">
            <button class="btn btn-primary w-full mb-12" (click)="reset()">New Recharge</button>
            <div class="secondary-btns">
              <button class="btn btn-outline-gold" (click)="downloadReceipt()">
                <lucide-icon name="download" [size]="16" class="mr-8"></lucide-icon> Get PDF Receipt
              </button>
              <button class="btn btn-ghost" (click)="router.navigate(['/history'])">View History</button>
            </div>
          </div>
        </div>
      </div>

    </div>
  `,
  styles: [`
    .recharge-page { max-width: 900px; margin: 0 auto; padding-top: 20px; }

    .recharge-stepper {
      display: flex; align-items: center; justify-content: center; gap: 12px; margin-bottom: 48px;
      .step-indicator {
        width: 32px; height: 32px; border-radius: 50%; border: 2px solid var(--glass-border);
        display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 800;
        color: var(--text-muted); transition: all 0.3s;
        &.active { border-color: var(--accent-gold); color: var(--accent-gold); box-shadow: var(--glow-gold); }
        &.done { background: var(--accent-emerald); border-color: var(--accent-emerald); color: white; }
      }
      .step-line { width: 40px; height: 2px; background: var(--glass-border); border-radius: 2px; }
    }

    .content-header { text-align: center; margin-bottom: 40px; }
    .content-header h1 { font-size: 32px; font-weight: 800; color: var(--text-primary); margin: 0; letter-spacing: -1px; }
    .content-header h1 span { color: var(--accent-gold); }
    .content-header p { color: var(--text-secondary); margin-top: 8px; font-size: 16px; }

    .main-input-card { max-width: 600px; margin: 0 auto; padding: 40px; }

    .premium-input-group {
      display: flex; align-items: center; gap: 16px; padding: 20px 28px;
      background: rgba(255, 255, 255, 0.03); border: 1px solid var(--glass-border);
      border-radius: 24px; transition: all 0.4s ease;
      
      &.is-invalid { 
        border-color: #f43f5e; 
        background: rgba(244, 63, 94, 0.03);
        box-shadow: 0 0 20px rgba(244, 63, 94, 0.1);
      }
      &.is-valid { 
        border-color: #10b981; 
        background: rgba(16, 185, 129, 0.03);
      }
      &:focus-within:not(.is-invalid) { border-color: var(--accent-gold); box-shadow: 0 0 0 8px rgba(225, 202, 150, 0.05); }
      
      input { 
        flex: 1; background: none; border: none; outline: none; 
        font-size: 26px; font-weight: 800; color: var(--text-primary); letter-spacing: 3px;
        &::placeholder { letter-spacing: 0; color: var(--text-muted); font-size: 18px; font-weight: 500; }
      }
      .input-status-tray { display: flex; align-items: center; margin-right: 8px; }
      .text-emerald { color: #10b981; }
      .text-rose { color: #f43f5e; }
    }

    .validation-feedback {
      margin-top: 12px;
      .error-pill {
        display: inline-flex; align-items: center; gap: 8px;
        background: rgba(244, 63, 94, 0.1); color: #fb7185;
        padding: 6px 16px; border-radius: 50px; font-size: 12px; font-weight: 700;
        border: 1px solid rgba(244, 63, 94, 0.2);
      }
    }

    /* Skeleton Loader Styles */
    .shimmer {
      background: linear-gradient(90deg, rgba(255,255,255,0.03) 25%, rgba(255,255,255,0.08) 50%, rgba(255,255,255,0.03) 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
    }
    @keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }

    .skeleton-op-card {
      display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 16px;
      .skeleton-circle { width: 56px; height: 56px; border-radius: 50%; }
      .skeleton-line { width: 60px; height: 10px; border-radius: 4px; }
    }

    .plan-card.skeleton {
      height: 200px;
      .skeleton-line-lg { width: 40%; height: 24px; border-radius: 8px; margin-bottom: 20px; }
      .skeleton-line-md { width: 100%; height: 14px; border-radius: 4px; margin-bottom: 12px; }
      .skeleton-line-sm { width: 80%; height: 14px; border-radius: 4px; }
    }

    @keyframes shake {
      10%, 90% { transform: translateX(-1px); }
      20%, 80% { transform: translateX(2px); }
      30%, 50%, 70% { transform: translateX(-4px); }
      40%, 60% { transform: translateX(4px); }
    }
    .shake { animation: shake 0.5s cubic-bezier(.36,.07,.19,.97) both; }
    .bounce-in { animation: bounceIn 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55); }
    @keyframes bounceIn { from { transform: scale(0); } to { transform: scale(1); } }


    .recent-hub { max-width: 600px; margin: 0 auto 32px;
      .rh-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; h3 { font-size: 14px; font-weight: 800; color: var(--text-muted); text-transform: uppercase; letter-spacing: 1px; } .btn-link { background: none; border: none; color: var(--accent-gold); font-weight: 700; font-size: 12px; cursor: pointer; } }
      .rh-list { display: flex; gap: 16px; overflow-x: auto; padding-bottom: 8px; scrollbar-width: none; &::-webkit-scrollbar { display: none; } }
      .rh-item { flex: 0 0 100px; text-align: center; cursor: pointer; transition: transform 0.3s; &:hover { transform: translateY(-5px); } }
      .rh-avatar { width: 56px; height: 56px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 8px; font-size: 20px; font-weight: 800; color: white; border: 2px solid rgba(255,255,255,0.1); }
      .rh-name { display: block; font-size: 13px; font-weight: 700; color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
      .rh-num { display: block; font-size: 11px; color: var(--text-muted); }
    }

    .detection-badge {
      display: flex; justify-content: space-between; align-items: center; margin-top: 16px;
      background: rgba(16, 185, 129, 0.05); padding: 12px 20px; border-radius: 16px; border: 1px solid rgba(16, 185, 129, 0.1);
      .db-info { display: flex; align-items: center; gap: 12px; font-size: 13px; font-weight: 700; color: white; }
      .db-logo { width: 24px; height: 24px; border-radius: 50%; }
      .db-edit { background: none; border: none; color: var(--accent-gold); font-size: 12px; font-weight: 800; cursor: pointer; }
    }

    .operator-grid { 
      display: grid; grid-template-columns: repeat(auto-fill, minmax(100px, 1fr)); 
      gap: 20px; margin-top: 40px; 
    }
    .op-card {
      display: flex; flex-direction: column; align-items: center; gap: 12px;
      cursor: pointer; padding: 16px; border-radius: 20px; transition: all 0.3s;
      border: 1px solid transparent;
      &:hover { background: rgba(255, 255, 255, 0.03); transform: translateY(-5px); }
      &.selected { background: rgba(225, 202, 150, 0.08); border-color: var(--glass-border); }
    }
    .op-logo-wrap {
      width: 56px; height: 56px; border-radius: 50%; background: #121416;
      border: 1px solid var(--glass-border); display: flex; align-items: center; justify-content: center;
      overflow: hidden; box-shadow: var(--card-shadow);
      img { width: 100%; height: 100%; object-fit: cover; }
      span { font-size: 20px; font-weight: 800; color: var(--accent-gold); }
    }
    .op-name { font-size: 13px; font-weight: 700; color: var(--text-secondary); text-align: center; }

    .context-pill {
      display: flex; justify-content: space-between; align-items: center;
      background: var(--bg-card); padding: 12px 24px; border-radius: 50px;
      border: 1px solid var(--glass-border); margin-bottom: 32px;
      .cp-mobile { font-weight: 800; color: var(--text-primary); font-size: 16px; margin-right: 12px; }
      .cp-operator { color: var(--text-muted); font-size: 13px; font-weight: 600; }
      .cp-edit { background: none; border: none; color: var(--accent-gold); font-weight: 700; font-size: 13px; cursor: pointer; }
    }

    .plans-filter { display: flex; gap: 12px; margin-bottom: 24px; overflow-x: auto; padding-bottom: 8px; }
    .filter-chip {
      padding: 10px 20px; border-radius: 30px; font-size: 13px; font-weight: 700;
      background: rgba(255, 255, 255, 0.03); border: 1px solid var(--glass-border);
      color: var(--text-secondary); cursor: pointer; white-space: nowrap; transition: all 0.3s;
      &.active { background: var(--accent-gold); color: #121416; border-color: var(--accent-gold); }
    }

    .plans-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 24px; }
    .plan-card {
      padding: 24px; position: relative; border-radius: 28px;
      .plan-badge { 
        position: absolute; top: 12px; right: 12px; background: var(--accent-gold); 
        color: #121416; font-size: 10px; font-weight: 800; padding: 4px 8px; border-radius: 6px; text-transform: uppercase;
      }
      .plan-top { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 16px; }
      .p-amount { font-size: 32px; font-weight: 800; color: var(--text-primary); }
      .p-validity { font-size: 14px; font-weight: 700; color: var(--accent-gold); }
      .p-desc { font-size: 14px; color: var(--text-secondary); line-height: 1.6; margin-bottom: 24px; height: 44px; overflow: hidden; }
      .plan-footer { display: flex; gap: 12px; }
      .btn-sm { padding: 8px 16px; font-size: 12px; flex: 1; }
    }

    .checkout-view { max-width: 600px; margin: 0 auto; }
    .checkout-header { text-align: center; margin-bottom: 32px; }
    .back-link { 
      background: none; border: none; color: var(--text-muted); cursor: pointer; 
      display: flex; align-items: center; gap: 4px; font-size: 13px; font-weight: 600; margin: 0 auto 12px;
    }
    .summary-card { padding: 32px; border-radius: 28px; margin-bottom: 40px; }
    .s-row { display: flex; justify-content: space-between; margin-bottom: 16px; }
    .s-label { color: var(--text-muted); font-size: 14px; font-weight: 600; }
    .s-val { color: var(--text-primary); font-weight: 700; font-size: 15px; }
    .s-divider { height: 1px; background: var(--glass-border); margin: 20px 0; }
    .s-row.total .s-label { color: var(--text-primary); font-size: 18px; font-weight: 800; }
    .s-amount { color: var(--accent-gold); font-size: 28px; font-weight: 800; }

    .payment-section h3 { font-size: 16px; color: var(--text-primary); margin-bottom: 20px; span { color: var(--accent-gold); } }
    .method-list { display: flex; flex-direction: column; gap: 12px; margin-bottom: 40px; }
    .method-item {
      display: flex; align-items: center; gap: 16px; padding: 18px 24px;
      background: rgba(255, 255, 255, 0.02); border: 1px solid var(--glass-border);
      border-radius: 20px; cursor: pointer; transition: all 0.3s;
      .m-icon { color: var(--text-muted); }
      .m-label { flex: 1; font-weight: 700; font-size: 15px; color: var(--text-secondary); }
      .m-check { color: var(--accent-emerald); }
      &.active { 
        background: rgba(225, 202, 150, 0.05); border-color: var(--accent-gold); 
        .m-icon { color: var(--accent-gold); }
        .m-label { color: var(--text-primary); }
      }
    }

    .pay-button-container { min-height: 80px; margin-top: 32px; display: flex; align-items: center; justify-content: center; }
    .pay-btn { width: 100%; height: 64px; font-size: 18px; border-radius: 20px; position: relative; overflow: hidden; transition: all 0.3s; &:active { transform: scale(0.98); } }

    .success-view { display: flex; align-items: center; justify-content: center; min-height: 70vh; }
    .success-box { 
      width: 100%; max-width: 480px; padding: 48px; border-radius: 32px; 
      text-align: center; position: relative; overflow: hidden;
      h1 { font-size: 28px; font-weight: 800; margin: 24px 0 12px; }
      p { color: var(--text-secondary); font-size: 16px; }
    }
    .success-glow { 
      position: absolute; top: -50px; left: 50%; transform: translateX(-50%); 
      width: 200px; height: 200px; background: radial-gradient(circle, rgba(16, 185, 129, 0.15) 0%, transparent 70%); 
      filter: blur(40px);
    }
    .success-icon-wrap {
      width: 80px; height: 80px; background: rgba(16, 185, 129, 0.1);
      border-radius: 24px; display: flex; align-items: center; justify-content: center;
      margin: 0 auto; box-shadow: 0 0 30px rgba(16, 185, 129, 0.2);
      .emerald-icon { color: var(--accent-emerald); }
    }
    .details-list { margin: 32px 0; background: rgba(255, 255, 255, 0.02); border-radius: 20px; padding: 12px; }
    .d-item { 
      display: flex; justify-content: space-between; padding: 12px 16px; font-size: 13px;
      span { color: var(--text-muted); font-weight: 600; }
      strong { color: var(--text-primary); font-weight: 700; }
    }
    .success-footer { margin-top: 32px; }
    .secondary-btns { display: flex; flex-direction: column; gap: 12px; }
    .secondary-btns .btn { width: 100%; }
    .mb-12 { margin-bottom: 12px; }
    .mr-8 { margin-right: 8px; }

    .btn-outline-gold {
      background: none; border: 1px solid var(--accent-gold); color: var(--accent-gold);
      &:hover { background: rgba(225, 202, 150, 0.1); transform: translateY(-2px); box-shadow: var(--glow-gold); }
    }

    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
    @keyframes fadeInUp { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }
    @keyframes slideUp { from { opacity: 0; transform: translateY(60px); } to { opacity: 1; transform: translateY(0); } }
  `]
})
export class RechargeComponent implements OnInit {
  private fb          = inject(FormBuilder);
  private auth        = inject(AuthService);
  private operatorSvc = inject(OperatorService);
  private rechargeSvc = inject(RechargeService);
  private paymentSvc  = inject(PaymentService);
  private toast       = inject(ToastService);
  private ngZone      = inject(NgZone);
  public  router      = inject(Router);

  step             = signal(1); // 1: Mobile, 2: Operator, 3: Plans, 4: Checkout, 5: Success
  operators        = signal<Operator[]>([]);
  plans            = signal<Plan[]>([]);
  selectedOperator = signal<Operator | null>(null);
  selectedPlan     = signal<Plan | null>(null);
  detectedOperator = signal<Operator | null>(null);
  detectedCircle   = signal<string>('');
  paymentMethod    = signal('UPI');
  loading          = signal(false);
  
  lastTransactionId = signal('');
  currentTime       = signal('');
  mobileForm        = this.fb.group({
    mobile: ['', [Validators.required, Validators.pattern(/^[6-9]\d{9}$/)]]
  });

  paymentMethods = [
    { label: 'Google Pay / UPI', value: 'UPI', iconName: 'wallet' },
    { label: 'Credit / Debit Card', value: 'CARD', iconName: 'credit-card' },
    { label: 'Net Banking', value: 'NETBANKING', iconName: 'landmark' }
  ];

  recentRecharges = [
    { name: 'Mom', mobile: '9810123456', color: '#818cf8' },
    { name: 'Sister', mobile: '9820198765', color: '#fb7185' },
    { name: 'Self', mobile: '9945088221', color: '#e1ca96' }
  ];

  private circleMapping: { [key: string]: string } = {
    '9810': 'Delhi', '9811': 'Delhi', '9910': 'Delhi', '8800': 'Delhi',
    '9820': 'Mumbai', '9821': 'Mumbai', '9920': 'Mumbai', '8828': 'Mumbai',
    '9844': 'Karnataka', '9845': 'Karnataka', '9945': 'Karnataka', '8095': 'Karnataka',
    '9830': 'Kolkata', '9831': 'Kolkata', '9903': 'Kolkata',
    '9840': 'Chennai', '9841': 'Chennai', '9940': 'Chennai',
    '9822': 'Maharashtra', '9823': 'Maharashtra', '9922': 'Maharashtra',
    '9860': 'Maharashtra', '7028': 'Maharashtra', '8888': 'Maharashtra',
    '9415': 'UP (East)', '9450': 'UP (East)', '9935': 'UP (East)',
    '9839': 'UP (East)', '9151': 'UP (East)',
    '9838': 'UP (West)', '9917': 'UP (West)', '9412': 'UP (West)',
    '9848': 'Andhra Pradesh', '9849': 'Andhra Pradesh', '9948': 'Andhra Pradesh',
    '9895': 'Kerala', '9894': 'Tamil Nadu', '9801': 'Bihar', '9934': 'Bihar'
  };

  ngOnInit() {
    this.loadOperators();
  }

  loadOperators() {
    this.operatorSvc.getAll().subscribe({
      next: (data: Operator[]) => this.operators.set(data),
      error: (err: any) => console.error(err)
    });
  }

  onMobileInput() {
    const val = this.mobileForm.value.mobile || '';
    
    // Circle Detection Logic
    if (val.length >= 4) {
      const prefix = val.substring(0, 4);
      this.detectedCircle.set(this.circleMapping[prefix] || 'National Circle');
    } else {
      this.detectedCircle.set('');
    }

    if (val.length === 10 && this.mobileForm.valid) {
      const ops = this.operators();
      if (ops.length > 0) {
        // Find operator based on prefix or just random for demo
        const randomOp = ops[Math.floor(Math.random() * ops.length)];
        this.detectedOperator.set(randomOp);
        this.selectedOperator.set(randomOp);
        this.plans.set(randomOp.plans || []);
        
        // Auto advance after short delay for better UX
        setTimeout(() => {
          if (this.step() === 1) {
            this.step.set(3);
            this.toast.success(`Detected ${randomOp.name} - ${this.detectedCircle()}`);
          }
        }, 1200);
      }
    } else {
      this.detectedOperator.set(null);
    }
  }

  selectRecent(recent: any) {
    this.mobileForm.patchValue({ mobile: recent.mobile });
    this.onMobileInput();
  }

  openContacts() {
    this.toast.info('Opening Contacts (Simulation)...');
  }

  autoAdvanceIfValid() {
    // Deprecated in favor of onMobileInput
  }

  goToStep2(): void {
    if (this.mobileForm.valid) this.step.set(2);
    else this.mobileForm.markAllAsTouched();
  }

  selectOperator(op: Operator): void { 
    this.selectedOperator.set(op); 
    this.plans.set(op.plans || []);
    this.step.set(3);
  }

  selectPlan(plan: Plan): void {
    this.selectedPlan.set(plan);
    this.step.set(4);
  }

  goBack() {
    if (this.step() > 1 && this.step() < 5) this.step.set(this.step() - 1);
    else this.router.navigate(['/dashboard']);
  }

  submit(): void {
    const user = this.auth.currentUser();
    if (!user || !this.selectedOperator() || !this.selectedPlan()) return;
    this.loading.set(true);

    const amount = this.selectedPlan()!.amount;

    // Step 1: Create PENDING Recharge first to get a RechargeID
    this.rechargeSvc.initiate(user.id, {
      operatorId:    this.selectedOperator()!.id,
      planId:        this.selectedPlan()!.id,
      mobileNumber:  this.mobileForm.value.mobile!,
      paymentMethod: 'RAZORPAY'
    }).subscribe({
      next: (rechargeRes) => {
        const rechargeId = rechargeRes.id;
        
        // Step 2: Create Razorpay Order
        this.paymentSvc.createOrder(amount).subscribe({
          next: (orderRes) => {
            this.loading.set(false);
            
            const options = {
              key: environment.razorpayKey,
              amount: orderRes.amount,
              currency: orderRes.currency,
              name: 'RechargeHub',
              description: `Recharge for ${this.mobileForm.value.mobile}`,
              order_id: orderRes.orderId,
              handler: (response: any) => {
                this.ngZone.run(() => {
                  // Step 3: Verify and complete
                  this.verifyPaymentAndRecharge(response, user, rechargeId);
                });
              },
              prefill: {
                name: user.name,
                email: user.email,
                contact: this.mobileForm.value.mobile
              },
              theme: { color: '#E1CA96' },
              modal: {
                ondismiss: () => {
                  this.ngZone.run(() => {
                    this.loading.set(false);
                    this.toast.error('Payment cancelled');
                  });
                }
              }
            };

            const rzp = new Razorpay(options);
            rzp.open();
          },
          error: () => {
            this.loading.set(false);
            this.toast.error('Payment initialization failed.');
          }
        });
      },
      error: () => {
        this.loading.set(false);
        this.toast.error('Failed to initiate recharge.');
      }
    });
  }

  private verifyPaymentAndRecharge(paymentResponse: any, user: any, rechargeId: number) {
    this.loading.set(true);
    
    // Attach metadata for backend processing
    const verificationPayload = {
      ...paymentResponse,
      userId:     user.id,
      rechargeId: rechargeId,
      amount:     this.selectedPlan()!.amount,
      email:      user.email,
      mobile:     this.mobileForm.value.mobile,
      operator:   this.selectedOperator()!.name
    };

    this.paymentSvc.verifyPayment(verificationPayload).subscribe({
      next: (verifyRes) => {
        this.loading.set(false);
        if (verifyRes.status === 'SUCCESS') {
          this.lastTransactionId.set(rechargeId.toString());
          this.currentTime.set(new Date().toLocaleString());
          this.step.set(5);
          this.toast.success('Recharge Successful!');
        } else {
          this.toast.error('Payment verification failed.');
        }
      },
      error: () => {
        this.loading.set(false);
        this.toast.error('Verification failed. Contact support if amount was debited.');
      }
    });
  }

  downloadReceipt(): void {
    const tid = this.lastTransactionId();
    if (!tid) return;
    
    this.toast.info('Preparing your receipt...');
    
    // Construct event-like object for the backend PDF generator
    const receiptData = {
      transactionId: tid,
      rechargeId: tid, // Using tid as placeholder for ID
      amount: this.selectedPlan()?.amount,
      mobileNumber: this.mobileForm.value.mobile,
      operatorName: this.selectedOperator()?.name,
      status: 'SUCCESS'
    };

    this.rechargeSvc.downloadReceipt(receiptData).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Receipt-${tid}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.toast.success('Receipt downloaded successfully.');
      },
      error: () => this.toast.error('Failed to download receipt.')
    });
  }

  reset(): void {
    this.step.set(1); 
    this.selectedOperator.set(null); 
    this.selectedPlan.set(null);
    this.paymentMethod.set('UPI'); 
    this.mobileForm.reset();
    this.router.navigate(['/dashboard']);
  }
}
