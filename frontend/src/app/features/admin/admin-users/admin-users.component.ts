import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { ToastService } from '../../../core/services/toast.service';
import { LucideAngularModule } from 'lucide-angular';
import { RechargeResponse } from '../../../core/models/recharge.model';

interface UserDto {
  id: number;
  email: string;
  name: string;
  role: string;
}

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  template: `
    <div class="fade-in p-4">
      <div class="header-actions">
        <div>
          <h2 class="platform-title">Platform Oversight</h2>
          <p class="subtitle text-muted">MANAGEMENT & ANALYTICS</p>
        </div>
        <div class="stats-badge shadow-lg">
          <lucide-icon name="users" [size]="14" class="mr-2"></lucide-icon>
          Total: {{ users().length }} Users
        </div>
      </div>

      <div class="table-container glass mt-6 shadow-2xl">
        <table class="admin-table">
          <thead>
            <tr>
              <th>USER ID</th>
              <th>NAME</th>
              <th>EMAIL ADDRESS</th>
              <th>ROLE</th>
              <th class="text-right">ACTIONS</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngIf="loading()">
              <td colspan="5" class="text-center py-16">
                <div class="loader-container">
                  <lucide-icon name="loader-2" [size]="32" class="spin text-gold mx-auto mb-2"></lucide-icon>
                  <p class="text-muted text-sm">Fetching users...</p>
                </div>
              </td>
            </tr>
            <tr *ngIf="!loading() && users().length === 0">
              <td colspan="5" class="text-center py-16">
                <div class="empty-state">
                  <lucide-icon name="user-x" [size]="48" class="text-muted mx-auto mb-4 opacity-20"></lucide-icon>
                  <p class="text-muted">No users found on the platform.</p>
                </div>
              </td>
            </tr>
            <tr *ngFor="let user of users(); trackBy: trackById" class="user-row">
              <td class="text-muted font-mono text-xs">#USR-{{ user.id }}</td>
              <td>
                <div class="name-cell">
                  <div class="avatar-mini">{{ user.name ? user.name.charAt(0).toUpperCase() : '?' }}</div>
                  <span class="font-bold text-white">{{ user.name || 'N/A' }}</span>
                </div>
              </td>
              <td class="text-blue-gray">{{ user.email }}</td>
              <td>
                <span class="badge" [ngClass]="user.role === 'ROLE_ADMIN' ? 'badge-admin' : 'badge-user'">
                  {{ (user.role + '')?.replace('ROLE_', '') || 'USER' }}
                </span>
              </td>
              <td class="text-right">
                <div class="action-btns justify-end">
                  <button class="btn-icon insights" (click)="viewUserInsights(user)" title="View Insights">
                    <lucide-icon name="bar-chart-3" [size]="18"></lucide-icon>
                  </button>
                  <button class="btn-icon delete" (click)="deleteUser(user.id)" title="Delete User">
                    <lucide-icon name="trash-2" [size]="18"></lucide-icon>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .admin-users-container { padding: 10px 0; }
    .header-actions { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; h2 { margin: 0; color: white; font-weight: 800; font-size: 24px; } }
    
    .glass { background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.05); backdrop-filter: blur(10px); }
    .table-container { border-radius: 24px; overflow: hidden; position: relative; }
    
    .admin-table { width: 100%; border-collapse: collapse; text-align: left; }
    .admin-table th { padding: 20px 24px; background: rgba(255,255,255,0.03); color: #94a3b8; font-weight: 700; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; border-bottom: 1px solid rgba(255,255,255,0.05); }
    .admin-table td { padding: 16px 24px; color: #cbd5e1; border-bottom: 1px solid rgba(255,255,255,0.03); font-size: 14px; }
    .user-row:hover { background: rgba(255,255,255,0.01); }

    .user-cell { display: flex; align-items: center; gap: 12px; }
    .avatar-mini { width: 36px; height: 36px; background: linear-gradient(135deg, var(--accent-gold), #b39a65); color: #121416; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-weight: 800; }
    .user-name { display: block; font-weight: 700; color: white; }
    .user-id { font-size: 11px; color: #64748b; font-family: monospace; }

    .contact-cell { display: flex; flex-direction: column; gap: 4px; }
    .contact-item { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #94a3b8; lucide-icon { color: #64748b; } }

    .status-pill { padding: 4px 10px; border-radius: 6px; font-size: 11px; font-weight: 700; background: rgba(245, 158, 11, 0.1); color: #f59e0b; }
    .status-pill.verified { background: rgba(16, 185, 129, 0.1); color: #10b981; }

    .action-btns { display: flex; gap: 8px; &.justify-end { justify-content: flex-end; } }
    .btn-icon { background: none; border: none; color: #64748b; cursor: pointer; padding: 8px; border-radius: 10px; transition: all 0.2s; &:hover { background: rgba(255,255,255,0.05); color: white; } &.delete:hover { color: #ef4444; background: rgba(239,68,68,0.1); } &.insights:hover { color: var(--accent-gold); background: rgba(225,202,150,0.1); } }

    /* Insights Panel */
    .insights-backdrop { position: fixed; inset: 0; background: rgba(0,0,0,0.6); backdrop-filter: blur(4px); z-index: 1000; }
    .insights-panel { position: absolute; right: 0; top: 0; height: 100%; width: 100%; max-width: 450px; background: #121416; border-left: 1px solid rgba(255,255,255,0.1); display: flex; flex-direction: column; animation: slideIn 0.4s cubic-bezier(0.16, 1, 0.3, 1); }
    @keyframes slideIn { from { transform: translateX(100%); } to { transform: translateX(0); } }

    .panel-header { padding: 40px 32px; background: linear-gradient(to bottom, rgba(225, 202, 150, 0.05), transparent); .header-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; } .btn-close { background: none; border: none; color: #64748b; cursor: pointer; &:hover { color: white; } } .badge { background: rgba(225, 202, 150, 0.1); color: var(--accent-gold); padding: 4px 10px; border-radius: 6px; font-size: 10px; font-weight: 800; letter-spacing: 1px; } }
    .user-profile-summary { display: flex; align-items: center; gap: 20px; .avatar-large { width: 64px; height: 64px; background: linear-gradient(135deg, var(--accent-gold), #b39a65); color: #121416; border-radius: 20px; display: flex; align-items: center; justify-content: center; font-size: 24px; font-weight: 900; } .profile-text { h3 { color: white; margin: 0; font-size: 20px; font-weight: 800; } p { color: #64748b; margin: 2px 0 0; font-size: 13px; } } }

    .panel-body { flex: 1; overflow-y: auto; padding: 0 32px 40px; }
    .insight-metrics { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 32px; }
    .metric-card { padding: 20px; background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.05); border-radius: 20px; .m-label { font-size: 11px; font-weight: 800; color: #64748b; text-transform: uppercase; letter-spacing: 1px; } .m-val { margin: 8px 0 0; color: white; font-size: 22px; font-weight: 800; } }
    
    .history-section { .section-title { color: #cbd5e1; font-weight: 800; font-size: 14px; margin-bottom: 16px; display: flex; align-items: center; } }
    .txn-list { display: flex; flex-direction: column; gap: 12px; }
    .txn-item { padding: 16px; background: rgba(255,255,255,0.01); border: 1px solid rgba(255,255,255,0.03); border-radius: 16px; display: flex; justify-content: space-between; align-items: center; }
    .txn-main { display: flex; align-items: center; gap: 12px; .txn-icon { width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; &.success { background: rgba(16, 185, 129, 0.1); color: #10b981; } &.failed { background: rgba(239, 68, 68, 0.1); color: #ef4444; } } .txn-info { display: flex; flex-direction: column; .txn-amount { color: white; font-weight: 700; font-size: 14px; } .txn-date { font-size: 11px; color: #64748b; } } }
    .txn-status { font-size: 10px; font-weight: 800; text-transform: uppercase; &.success { color: #10b981; } &.failed { color: #ef4444; } }
    .empty-state { text-align: center; padding: 40px 0; color: #64748b; p { font-size: 13px; margin-top: 12px; } }

    .loading-overlay { position: absolute; inset: 0; background: rgba(18, 20, 22, 0.7); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 10; }
    .spinner { width: 40px; height: 40px; border: 3px solid rgba(225, 202, 150, 0.1); border-top-color: var(--accent-gold); border-radius: 50%; animation: spin 0.8s linear infinite; }
    .spinner-sm { width: 24px; height: 24px; border: 2px solid rgba(225, 202, 150, 0.1); border-top-color: var(--accent-gold); border-radius: 50%; animation: spin 0.8s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }
    .animate-spin { animation: spin 1s linear infinite; }
    
    .fade-in { animation: fadeIn 0.4s ease-out; }
    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
    .text-right { text-align: right; }
  `]
})
export class AdminUsersComponent implements OnInit {
  private http = inject(HttpClient);
  private toast = inject(ToastService);

  users = signal<any[]>([]);
  loading = signal(false);
  
  // User Insights State
  selectedUser = signal<any | null>(null);
  userTransactions = signal<RechargeResponse[]>([]);
  loadingTransactions = signal(false);
  showInsights = signal(false);

  ngOnInit() {
    this.fetchUsers();
  }

  fetchUsers() {
    this.loading.set(true);
    this.http.get<any[]>(`${environment.apiUrl}/api/users/admin/all`).subscribe({
      next: (data) => {
        this.users.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Fetch Users Error:', err);
        this.loading.set(false);
      }
    });
  }

  viewUserInsights(user: any) {
    this.selectedUser.set(user);
    this.showInsights.set(true);
    this.loadingTransactions.set(true);
    this.userTransactions.set([]);

    this.http.get<RechargeResponse[]>(`${environment.apiUrl}/api/recharges/user/${user.id}`).subscribe({
      next: (data) => {
        this.userTransactions.set(data);
        this.loadingTransactions.set(false);
      },
      error: (err) => {
        console.error('Fetch Transactions Error:', err);
        this.loadingTransactions.set(false);
        this.toast.error('Could not load user activity');
      }
    });
  }

  get totalSpent(): number {
    return this.userTransactions()
      .filter(t => t.status === 'SUCCESS')
      .reduce((acc, t) => acc + t.amount, 0);
  }

  get successRate(): number {
    const total = this.userTransactions().length;
    if (total === 0) return 0;
    const success = this.userTransactions().filter(t => t.status === 'SUCCESS').length;
    return Math.round((success / total) * 100);
  }

  deleteUser(userId: number) {
    if (confirm('Are you sure you want to delete this user? This action cannot be undone.')) {
      this.http.delete(`${environment.apiUrl}/api/users/admin/${userId}`).subscribe({
        next: () => {
          this.users.update(prev => prev.filter(u => u.id !== userId));
          this.toast.success('User deleted successfully');
        },
        error: () => this.toast.error('Failed to delete user')
      });
    }
  }

  trackById(index: number, item: any) {
    return item.id;
  }
}
