import { Component, OnInit, OnDestroy, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { NotificationResponse } from '../../core/models/recharge.model';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page">
      <div class="page-header">
        <div>
          <h2>🔔 Notifications</h2>
          <p>Your recharge alerts and updates</p>
        </div>
        <div class="header-right">
          <span class="poll-indicator" title="Auto-refreshes every 30s">🔄 Live</span>
          <span class="count-badge" *ngIf="notifications().length > 0">
            {{ notifications().length }} total
          </span>
        </div>
      </div>

      <div class="notif-list">
        <div class="empty-state" *ngIf="notifications().length === 0">
          <div class="icon">🔔</div>
          <p>No notifications yet. Complete a recharge to receive notifications.</p>
        </div>

        <div class="notif-card" *ngFor="let n of notifications()">
          <div class="notif-left">
            <div class="notif-dot" [class]="n.status.toLowerCase()"></div>
            <div>
              <p class="notif-msg">{{ n.message }}</p>
              <span class="notif-time">{{ n.createdAt | date:'dd MMM yyyy, hh:mm a' }}</span>
            </div>
          </div>
          <div class="notif-right">
            <span class="badge" [class]="n.status.toLowerCase()">{{ n.status }}</span>
            <span class="type-badge">{{ n.type }}</span>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .page { padding: 24px; max-width: 800px; margin: 0 auto; }
    .page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; flex-wrap: wrap; gap: 12px;
      h2 { font-size: 22px; font-weight: 700; color: var(--text-gold); }
      p  { color: var(--text-muted); font-size: 14px; margin-top: 4px; }
    }
    .header-right { display: flex; align-items: center; gap: 10px; }
    .poll-indicator { font-size: 12px; color: var(--accent-emerald); background: rgba(16, 185, 129, 0.1); padding: 4px 10px; border-radius: 20px; border: 1px solid rgba(16, 185, 129, 0.2); }
    .count-badge { font-size: 12px; color: var(--text-muted); background: var(--bg-card); padding: 4px 10px; border-radius: 20px; }
    .notif-list { display: flex; flex-direction: column; gap: 12px; }
    .notif-card {
      display: flex; justify-content: space-between; align-items: center; gap: 16px;
      background: var(--bg-card); border: 1px solid var(--glass-border);
      border-radius: 12px; padding: 16px; flex-wrap: wrap;
      transition: all 0.2s;
      &:hover { border-color: var(--accent-gold); background: rgba(255,255,255,0.07); }
    }
    .notif-left { display: flex; align-items: flex-start; gap: 12px; flex: 1; }
    .notif-dot { width: 10px; height: 10px; border-radius: 50%; margin-top: 5px; flex-shrink: 0;
      &.sent   { background: var(--accent-emerald); box-shadow: 0 0 8px rgba(16, 185, 129, 0.5); }
      &.failed { background: var(--accent-red); box-shadow: 0 0 8px rgba(244, 63, 94, 0.5); }
    }
    .notif-msg  { font-size: 14px; color: var(--text-secondary); line-height: 1.5; }
    .notif-time { font-size: 12px; color: var(--text-muted); margin-top: 4px; display: block; }
    .notif-right { display: flex; align-items: center; gap: 8px; }
    .badge { display: inline-block; padding: 3px 10px; border-radius: 20px; font-size: 11px; font-weight: 700; text-transform: uppercase;
      &.sent   { background: rgba(34,197,94,0.15);  color: #86efac; }
      &.failed { background: rgba(239,68,68,0.15);  color: #fca5a5; }
    }
    .type-badge { font-size: 11px; color: var(--text-muted); background: rgba(255,255,255,0.05); padding: 3px 8px; border-radius: 10px; }
    .empty-state { text-align: center; padding: 48px; color: #64748b;
      .icon { font-size: 48px; margin-bottom: 12px; }
      p { font-size: 15px; }
    }
  `]
})
export class NotificationsComponent implements OnInit, OnDestroy {
  private auth     = inject(AuthService);
  private notifSvc = inject(NotificationService);

  notifications = signal<NotificationResponse[]>([]);
  private pollSub?: Subscription;

  ngOnInit(): void {
    this.load();
    // Poll every 30 seconds
    this.pollSub = interval(30000).subscribe(() => this.load());
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }

  private load(): void {
    const userId = this.auth.currentUser()?.id;
    if (!userId) return;
    this.notifSvc.getByUserId(userId).subscribe({
      next: (d) => this.notifications.set(d),
      error: () => {}
    });
  }
}
