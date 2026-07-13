import { Component, OnInit, OnDestroy, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { RechargeService } from '../../core/services/recharge.service';
import { NotificationService } from '../../core/services/notification.service';
import { ToastService } from '../../core/services/toast.service';
import { ThemeService } from '../../core/services/theme.service';
import { RechargeResponse, NotificationResponse } from '../../core/models/recharge.model';
import { interval, Subscription } from 'rxjs';
import { LucideAngularModule, Smartphone, Zap, Tv, Wifi, Moon, Sun, Bell, Activity, History, ShieldAlert } from 'lucide-angular';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, LucideAngularModule],
  template: `
    <div class="page">
      <!-- Top Navigation & Theme Toggle -->
      <div class="top-nav">
        <div class="user-greeting">
          <div class="avatar">{{ user()?.name?.charAt(0) || 'U' }}</div>
          <div>
            <p class="greeting-time">Good Morning,</p>
            <h3>{{ user()?.name }}</h3>
          </div>
        </div>
        <div class="nav-actions">
          <button class="btn btn-icon-circle" (click)="themeSvc.toggleTheme()">
            <lucide-icon [name]="themeSvc.isDarkMode() ? 'sun' : 'moon'" [size]="20"></lucide-icon>
          </button>
          <a routerLink="/notifications" class="btn btn-icon-circle notif-bell">
            <lucide-icon name="bell" [size]="20"></lucide-icon>
            <span class="badge-dot" *ngIf="notifications().length > 0"></span>
          </a>
        </div>
      </div>

      <!-- Hero Banner (PhonePe/Paytm modern style) -->
      <div class="hero-card">
        <div class="hero-content">
          <div class="balance-sec">
            <p>Wallet Balance</p>
            <h2>₹0.00</h2>
          </div>
          <button class="btn btn-primary topup-btn" routerLink="/recharge">Top Up</button>
        </div>
      </div>

      <!-- Circular Services Grid (Recharge & Pay Bills) -->
      <div class="services-section">
        <h3>Recharge & Pay Bills</h3>
        <div class="services-grid">
          <a routerLink="/recharge" class="service-item">
            <div class="icon-circle mobile-icon">
              <lucide-icon name="smartphone" [size]="24"></lucide-icon>
            </div>
            <span>Mobile</span>
          </a>
          <div class="service-item" (click)="comingSoon('DTH')">
            <div class="icon-circle dth-icon">
              <lucide-icon name="tv" [size]="24"></lucide-icon>
            </div>
            <span>DTH</span>
          </div>
          <div class="service-item" (click)="comingSoon('Electricity')">
            <div class="icon-circle elec-icon">
              <lucide-icon name="zap" [size]="24"></lucide-icon>
            </div>
            <span>Electricity</span>
          </div>
          <div class="service-item" (click)="comingSoon('Broadband')">
            <div class="icon-circle wifi-icon">
              <lucide-icon name="wifi" [size]="24"></lucide-icon>
            </div>
            <span>Broadband</span>
          </div>
        </div>
      </div>

      <!-- Quick Stats -->
      <div class="stats-row">
        <div class="stat-card">
          <lucide-icon name="activity" class="stat-icon purple"></lucide-icon>
          <div><div class="stat-value">{{ recharges().length }}</div><div class="stat-label">Total Recharges</div></div>
        </div>
        <div class="stat-card">
          <lucide-icon name="history" class="stat-icon green"></lucide-icon>
          <div><div class="stat-value">{{ successCount() }}</div><div class="stat-label">Successful</div></div>
        </div>
        <div class="stat-card" *ngIf="isAdmin()">
          <lucide-icon name="shield-alert" class="stat-icon yellow"></lucide-icon>
          <div><div class="stat-value">ADMIN</div><div class="stat-label">Access Level</div></div>
        </div>
      </div>

      <!-- Recent Recharges -->
      <div class="card section hoverable">
        <div class="section-header">
          <h3>Recent Recharges</h3>
          <a routerLink="/history" class="view-all">View all →</a>
        </div>
        <div class="empty-state" *ngIf="recharges().length === 0 && !loading()">
          <lucide-icon name="smartphone" [size]="48" class="icon"></lucide-icon>
          <p>No recharges yet. <a routerLink="/recharge">Start your first recharge!</a></p>
        </div>
        <table *ngIf="recharges().length > 0">
          <thead><tr><th>Mobile</th><th>Amount</th><th>Status</th><th>Date</th></tr></thead>
          <tbody>
            <tr *ngFor="let r of recharges().slice(0,5)">
                <td>{{ r.mobileNumber }}</td>
                <td><strong class="text-emerald">₹{{ r.amount }}</strong></td>
                <td><span class="badge" [class]="r.status.toLowerCase()">{{ r.status }}</span></td>
                <td>{{ r.createdAt | date:'dd MMM, hh:mm a' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .page { padding: 24px; max-width: 800px; margin: 0 auto; }
    
    .top-nav { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
    .user-greeting { display: flex; align-items: center; gap: 12px; }
    .avatar { width: 44px; height: 44px; border-radius: 50%; background: var(--gradient-gold); color: #121416; display: flex; align-items: center; justify-content: center; font-size: 20px; font-weight: 700; box-shadow: var(--card-shadow); }
    .greeting-time { font-size: 12px; color: var(--text-muted); margin-bottom: 2px; }
    .user-greeting h3 { font-size: 18px; font-weight: 700; color: var(--text-gold); }
    
    .nav-actions { display: flex; gap: 12px; }
    .notif-bell { position: relative; }
    .badge-dot { position: absolute; top: 12px; right: 12px; width: 8px; height: 8px; border-radius: 50%; background: var(--accent-red); border: 2px solid var(--bg-card); }

    .hero-card {
      background: var(--gradient-gold);
      border-radius: 20px;
      padding: 28px 32px;
      margin-bottom: 32px;
      color: #121416;
      box-shadow: var(--glow-gold);
      position: relative;
      overflow: hidden;
    }
    .hero-card::after {
      content: ''; position: absolute; top: -50%; right: -20%; width: 300px; height: 300px; background: rgba(255,255,255,0.1); border-radius: 50%;
    }
    .hero-content { display: flex; justify-content: space-between; align-items: center; position: relative; z-index: 1; flex-wrap: wrap; gap: 16px; }
    .balance-sec p { font-size: 14px; opacity: 0.9; margin-bottom: 4px; }
    .balance-sec h2 { font-size: 32px; font-weight: 700; }
    .topup-btn { background: rgba(255,255,255,0.2); border: 1px solid rgba(255,255,255,0.3); color: white; backdrop-filter: blur(4px); box-shadow: none; }
    .topup-btn:hover { background: rgba(255,255,255,0.3); }

    .services-section { margin-bottom: 32px; }
    .services-section h3 { font-size: 16px; font-weight: 600; color: var(--text-primary); margin-bottom: 16px; }
    .services-grid { display: flex; gap: 24px; flex-wrap: wrap; }
    .service-item { display: flex; flex-direction: column; align-items: center; gap: 8px; cursor: pointer; text-decoration: none; transition: transform 0.2s; }
    .service-item:hover { transform: translateY(-4px); }
    .service-item span { font-size: 13px; font-weight: 500; color: var(--text-secondary); }
    
    .icon-circle { width: 64px; height: 64px; border-radius: 20px; display: flex; align-items: center; justify-content: center; background: var(--bg-card); border: 1px solid var(--glass-border); box-shadow: var(--card-shadow); color: white; }
    .mobile-icon { background: linear-gradient(135deg, #3b82f6, #2563eb); }
    .dth-icon { background: linear-gradient(135deg, #f59e0b, #d97706); }
    .elec-icon { background: linear-gradient(135deg, #10b981, #059669); }
    .wifi-icon { background: linear-gradient(135deg, #8b5cf6, #6d28d9); }

    .stats-row { display: grid; grid-template-columns: repeat(auto-fit,minmax(180px,1fr)); gap: 16px; margin-bottom: 32px; }
    .stat-card { background: var(--bg-card); border: 1px solid var(--glass-border); border-radius: 16px; padding: 20px; display: flex; align-items: center; gap: 16px; box-shadow: var(--card-shadow); transition: transform 0.2s; }
    .stat-card:hover { transform: translateY(-2px); }
    .stat-icon { width: 44px; height: 44px; padding: 10px; border-radius: 12px; }
    .stat-icon.purple { background: rgba(124,58,237,0.1); color: var(--accent-purple); }
    .stat-icon.green { background: rgba(34,197,94,0.1); color: var(--accent-green); }
    .stat-icon.yellow { background: rgba(245,158,11,0.1); color: var(--accent-yellow); }
    
    .stat-value { font-size: 24px; font-weight: 700; color: var(--text-primary); }
    .stat-label { font-size: 12px; color: var(--text-muted); margin-top: 2px; }

    .section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
    .section-header h3 { font-size: 16px; font-weight: 600; color: var(--text-primary); }
    .view-all { font-size: 13px; font-weight: 600; color: var(--text-gold); }
  `]
})
export class DashboardComponent implements OnInit, OnDestroy {
  public auth        = inject(AuthService);
  private rechargeSvc = inject(RechargeService);
  private notifSvc    = inject(NotificationService);
  private toast       = inject(ToastService);
  public themeSvc    = inject(ThemeService);

  user          = this.auth.currentUser;
  recharges     = signal<RechargeResponse[]>([]);
  notifications = signal<NotificationResponse[]>([]);
  successCount  = signal(0);
  loading       = signal(true);
  isAdmin       = () => this.auth.isAdmin();

  private pollSub?: Subscription;

  ngOnInit(): void {
    this.loadData();
    // Poll notifications every 30 seconds
    this.pollSub = interval(30000).subscribe(() => this.loadNotifications());
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }

  private loadData(): void {
    const userId = this.user()?.id;
    if (!userId) return;

    this.rechargeSvc.getByUserId(userId).subscribe({
      next: (d) => {
        this.recharges.set(d);
        this.successCount.set(d.filter(r => r.status === 'SUCCESS').length);
        this.loading.set(false);
      },
      error: () => { this.loading.set(false); }
    });

    this.loadNotifications();
  }

  private loadNotifications(): void {
    const userId = this.user()?.id;
    if (!userId) return;

    const prevCount = this.notifications().length;
    this.notifSvc.getByUserId(userId).subscribe({
      next: (d) => {
        this.notifications.set(d);
        if (d.length > prevCount) {
          this.toast.info(`${d.length - prevCount} new notification(s)`);
        }
      },
      error: () => {}
    });
  }

  comingSoon(service: string) {
    this.toast.info(`${service} service is coming soon!`);
  }
}
