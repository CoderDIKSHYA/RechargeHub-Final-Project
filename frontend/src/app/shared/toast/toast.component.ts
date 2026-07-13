import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../core/services/toast.service';
import { trigger, transition, style, animate } from '@angular/animations';

/**
 * Global toast container — placed once in layout.component.
 * Displays success/error/info toasts with slide-in animation.
 */
@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  animations: [
    trigger('slideIn', [
      transition(':enter', [
        style({ transform: 'translateX(100%)', opacity: 0 }),
        animate('250ms ease-out', style({ transform: 'translateX(0)', opacity: 1 }))
      ]),
      transition(':leave', [
        animate('200ms ease-in', style({ transform: 'translateX(100%)', opacity: 0 }))
      ])
    ])
  ],
  template: `
    <div class="toast-container">
      <div
        *ngFor="let t of toastSvc.toasts()"
        [@slideIn]
        class="toast"
        [class]="t.type"
        (click)="toastSvc.remove(t.id)">
        <span class="toast-icon">
          {{ t.type === 'success' ? '✅' : t.type === 'error' ? '❌' : 'ℹ️' }}
        </span>
        <span class="toast-msg">{{ t.message }}</span>
      </div>
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      top: 20px;
      right: 20px;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 10px;
      max-width: 360px;
    }
    .toast {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 14px 18px;
      border-radius: 10px;
      font-size: 14px;
      font-weight: 500;
      cursor: pointer;
      backdrop-filter: blur(12px);
      box-shadow: 0 8px 24px rgba(0,0,0,0.3);
      border: 1px solid rgba(255,255,255,0.1);

      &.success { background: rgba(34,197,94,0.2);  color: #86efac; border-color: rgba(34,197,94,0.3); }
      &.error   { background: rgba(239,68,68,0.2);  color: #fca5a5; border-color: rgba(239,68,68,0.3); }
      &.info    { background: rgba(6,182,212,0.2);  color: #67e8f9; border-color: rgba(6,182,212,0.3); }
    }
    .toast-icon { font-size: 16px; flex-shrink: 0; }
    .toast-msg  { flex: 1; line-height: 1.4; }
  `]
})
export class ToastComponent {
  toastSvc = inject(ToastService);
}
