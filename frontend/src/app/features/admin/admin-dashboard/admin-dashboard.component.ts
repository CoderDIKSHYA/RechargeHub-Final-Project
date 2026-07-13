import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import 'chart.js/auto';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { RechargeResponse } from '../../../core/models/recharge.model';
import { forkJoin } from 'rxjs';
import { RouterLink } from '@angular/router';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, LucideAngularModule, BaseChartDirective, RouterLink],
  template: `
    <div class="dashboard-container fade-in">
      <div class="header-main mb-8">
        <div>
          <h2 class="platform-title">Executive Dashboard <span *ngIf="loading()" style="font-size: 14px; font-weight: normal; color: #64748b; margin-left: 10px;">(Loading Data...)</span></h2>
          <p class="subtitle text-muted">PLATFORM OVERVIEW & CONTROLS</p>
        </div>
        <div class="header-actions">
          <button class="btn-report shadow-lg mr-4" (click)="exportToCSV()">
            <lucide-icon name="download" [size]="18" class="mr-2"></lucide-icon>
            Download Report
          </button>
          <button class="btn-broadcast shadow-lg" (click)="showBroadcastModal.set(true)">
            <lucide-icon name="megaphone" [size]="18" class="mr-2"></lucide-icon>
            Broadcast Alert
          </button>
        </div>
      </div>

      <!-- Top KPIs -->
      <div class="stats-row">
        <div class="stat-card glass">
          <div class="s-info">
            <span class="s-label">Total Users</span>
            <h2 class="s-val">{{ totalUsers() | number }}</h2>
            <div class="s-trend up"><lucide-icon name="trending-up" [size]="14"></lucide-icon> Platform Growth</div>
          </div>
          <div class="s-icon" style="background: rgba(16, 185, 129, 0.2)"><lucide-icon name="users" [size]="20" class="text-emerald"></lucide-icon></div>
        </div>
        
        <div class="stat-card glass">
          <div class="s-info">
            <span class="s-label">Total Recharges</span>
            <h2 class="s-val">{{ totalRecharges() | number }}</h2>
            <div class="s-trend up"><lucide-icon name="trending-up" [size]="14"></lucide-icon> Successful: {{ successCount() }}</div>
          </div>
          <div class="s-icon" style="background: rgba(99, 102, 241, 0.2)"><lucide-icon name="zap" [size]="20" style="color: #818cf8"></lucide-icon></div>
        </div>

        <div class="stat-card glass">
          <div class="s-info">
            <span class="s-label">Total Revenue</span>
            <h2 class="s-val text-gold">₹ {{ totalRevenue() | number:'1.0-2' }}</h2>
            <div class="s-trend up"><lucide-icon name="trending-up" [size]="14"></lucide-icon> Gross Collections</div>
          </div>
          <div class="s-icon" style="background: rgba(225, 202, 150, 0.2)"><lucide-icon name="indian-rupee" [size]="20" class="text-gold"></lucide-icon></div>
        </div>

        <div class="stat-card glass">
          <div class="s-info">
            <span class="s-label">Success Rate</span>
            <h2 class="s-val">{{ successRate() }}%</h2>
            <div class="s-trend" [ngClass]="successRate() > 90 ? 'up' : 'down'">
              <lucide-icon [name]="successRate() > 90 ? 'trending-up' : 'trending-down'" [size]="14"></lucide-icon> 
              Platform Performance
            </div>
          </div>
          <div class="s-icon" style="background: rgba(244, 63, 94, 0.2)"><lucide-icon name="activity" [size]="20" style="color: #fb7185"></lucide-icon></div>
        </div>
      </div>

      <!-- Main Charts Row -->
      <div class="charts-row">
        <!-- Transaction Trends Line Chart -->
        <div class="chart-card glass span-2 shadow-2xl">
          <div class="card-header">
            <h3>Transaction Activity (Last 7 Days)</h3>
            <div class="badge-gold">Live Feed</div>
          </div>
          <div class="chart-wrapper">
            <canvas baseChart
              [data]="lineChartData"
              [options]="lineChartOptions"
              [type]="'line'">
            </canvas>
          </div>
        </div>

        <!-- Payment Methods Doughnut -->
        <div class="chart-card glass shadow-2xl">
          <div class="card-header">
            <h3>Success Distribution</h3>
          </div>
          <div class="chart-wrapper doughnut-wrapper">
            <canvas baseChart
              [data]="doughnutChartData"
              [options]="doughnutChartOptions"
              [type]="'doughnut'">
            </canvas>
          </div>
        </div>
      </div>

      <!-- Secondary Metrics Row -->
      <div class="charts-row">
        <!-- Operator Performance Bar Chart -->
        <div class="chart-card glass span-2 shadow-2xl">
          <div class="card-header">
            <h3>Revenue by Operator</h3>
            <span class="text-muted text-xs">Top 5 Performers</span>
          </div>
          <div class="chart-wrapper">
            <canvas baseChart
              [data]="barChartData"
              [options]="barChartOptions"
              [type]="'bar'">
            </canvas>
          </div>
        </div>

        <!-- System Health Monitor -->
        <div class="chart-card glass shadow-2xl">
          <div class="card-header">
            <h3>System Monitor</h3>
            <div class="pulse-red" *ngIf="rechargeSvcStatus() === 'offline'"></div>
            <div class="pulse-green" *ngIf="rechargeSvcStatus() === 'online'"></div>
          </div>
          <div class="funnel-container">
            <div class="status-item">
              <div class="s-label-group">
                <lucide-icon name="user" [size]="14" class="mr-2"></lucide-icon>
                <span>User Service</span>
              </div>
              <div class="status-dot" [ngClass]="userSvcStatus()"></div>
            </div>
            <div class="status-item">
              <div class="s-label-group">
                <lucide-icon name="zap" [size]="14" class="mr-2"></lucide-icon>
                <span>Recharge Service</span>
              </div>
              <div class="status-dot" [ngClass]="rechargeSvcStatus()"></div>
            </div>
            <div class="status-item">
              <div class="s-label-group">
                <lucide-icon name="database" [size]="14" class="mr-2"></lucide-icon>
                <span>Operator Service</span>
              </div>
              <div class="status-dot" [ngClass]="operatorSvcStatus()"></div>
            </div>
             <div class="status-item mt-4 pt-4 border-t">
              <div class="s-label-group">
                <lucide-icon name="smartphone" [size]="14" class="mr-2"></lucide-icon>
                <span>Active Operators</span>
              </div>
              <span class="text-gold font-bold">{{ operatorCount() }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Recent Transactions Table -->
      <div class="chart-card glass shadow-2xl mt-6">
        <div class="card-header">
          <h3>Recent Platform Activity</h3>
          <button class="btn-ghost btn-sm text-gold" routerLink="/admin/history">View All <lucide-icon name="arrow-right" [size]="14" class="ml-1"></lucide-icon></button>
        </div>
        <div class="mini-table-wrapper">
          <table class="mini-table">
            <thead>
              <tr>
                <th>TXN ID</th>
                <th>MOBILE</th>
                <th>OPERATOR</th>
                <th>AMOUNT</th>
                <th>STATUS</th>
                <th>TIME</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let txn of recentRecharges()">
                <td class="font-mono text-xs">{{ txn.transactionId || 'RH-MOCK' }}</td>
                <td class="font-bold text-white">{{ txn.mobileNumber }}</td>
                <td>
                  <span class="text-muted text-xs">OPR-{{ txn.operatorId }}</span>
                </td>
                <td class="text-gold font-bold">₹{{ txn.amount }}</td>
                <td>
                  <span class="status-pill" [ngClass]="txn.status.toLowerCase()">{{ txn.status }}</span>
                </td>
                <td class="text-muted text-xs">{{ txn.createdAt | date:'hh:mm a' }}</td>
              </tr>
              <tr *ngIf="recentRecharges().length === 0">
                <td colspan="6" class="text-center py-8 text-muted">No recent activity detected.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Broadcast Modal -->
      <div class="modal-overlay" *ngIf="showBroadcastModal()" (click)="showBroadcastModal.set(false)">
        <div class="broadcast-modal glass fade-in-up" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <div class="flex items-center gap-3">
              <div class="icon-circle gold">
                <lucide-icon name="megaphone" [size]="20"></lucide-icon>
              </div>
              <div>
                <h3>Broadcast Announcement</h3>
                <p class="text-xs text-muted">Message will be visible to all platform users</p>
              </div>
            </div>
            <button class="btn-close" (click)="showBroadcastModal.set(false)">
              <lucide-icon name="x" [size]="20"></lucide-icon>
            </button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>Message Content</label>
              <textarea #broadcastMsg class="form-control" placeholder="Type your announcement here..." rows="4"></textarea>
            </div>
            <div class="form-group">
              <label>Alert Level</label>
              <div class="alert-selector">
                <button [class.active]="broadcastType() === 'INFO'" (click)="broadcastType.set('INFO')">Info</button>
                <button [class.active]="broadcastType() === 'OFFER'" (click)="broadcastType.set('OFFER')">Offer</button>
                <button [class.active]="broadcastType() === 'URGENT'" (click)="broadcastType.set('URGENT')">Urgent</button>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" (click)="showBroadcastModal.set(false)">Discard</button>
            <button class="btn-send" (click)="sendBroadcast(broadcastMsg.value)">
              <lucide-icon name="send" [size]="16" class="mr-2"></lucide-icon>
              Dispatch Broadcast
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-container { padding: 24px; padding-bottom: 40px; }
    .header-main { display: flex; justify-content: space-between; align-items: center; }
    .header-actions { display: flex; align-items: center; gap: 12px; }
    .platform-title { font-size: 28px; font-weight: 900; color: white; margin: 0; letter-spacing: -1px; }
    .subtitle { font-size: 11px; font-weight: 800; letter-spacing: 2px; margin-top: 4px; color: #64748b; }

    .btn-broadcast { 
      background: linear-gradient(135deg, var(--accent-gold), #b39a65); color: #121416; padding: 12px 24px; border-radius: 14px; border: none; font-weight: 800; font-size: 14px; cursor: pointer; display: flex; align-items: center; transition: all 0.3s;
      box-shadow: 0 10px 20px -5px rgba(225, 202, 150, 0.3);
      &:hover { transform: translateY(-2px); box-shadow: 0 15px 25px -5px rgba(225, 202, 150, 0.4); }
    }

    .btn-report {
      background: rgba(225, 202, 150, 0.05); color: var(--accent-gold); border: 1px solid rgba(225, 202, 150, 0.3); 
      padding: 12px 24px; border-radius: 14px; font-weight: 800; font-size: 14px; cursor: pointer; 
      display: flex; align-items: center; transition: all 0.3s;
      &:hover { background: rgba(225, 202, 150, 0.1); border-color: var(--accent-gold); transform: translateY(-2px); }
    }

    .modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.8); backdrop-filter: blur(8px); z-index: 1000; display: flex; align-items: center; justify-content: center; }
    .broadcast-modal { width: 100%; max-width: 500px; border-radius: 30px; background: #121416; border: 1px solid rgba(255,255,255,0.05); overflow: hidden; }
    .modal-header { padding: 24px; display: flex; justify-content: space-between; align-items: flex-start; border-bottom: 1px solid rgba(255,255,255,0.05); 
      h3 { color: white; margin: 0; font-size: 18px; font-weight: 800; }
    }
    .icon-circle { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; &.gold { background: rgba(225, 202, 150, 0.1); color: var(--accent-gold); } }
    .btn-close { background: none; border: none; color: #64748b; cursor: pointer; padding: 4px; &:hover { color: white; } }
    .modal-body { padding: 24px; }
    .modal-footer { padding: 20px 24px; background: rgba(0,0,0,0.2); display: flex; justify-content: flex-end; gap: 12px; }
    
    .alert-selector { 
      display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin-top: 8px;
      button { background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.05); color: #64748b; padding: 10px; border-radius: 12px; font-weight: 700; cursor: pointer; transition: all 0.2s; &.active { background: var(--accent-gold); color: #121416; border-color: var(--accent-gold); } }
    }
    .form-group { label { display: block; color: #cbd5e1; font-size: 13px; font-weight: 700; margin-bottom: 8px; } }
    .form-control { width: 100%; background: rgba(0,0,0,0.3); border: 1px solid rgba(255,255,255,0.1); border-radius: 16px; color: white; padding: 16px; margin-bottom: 20px; font-size: 14px; outline: none; &:focus { border-color: var(--accent-gold); } }
    .btn-send { background: var(--accent-gold); color: #121416; border: none; padding: 12px 24px; border-radius: 14px; font-weight: 800; cursor: pointer; display: flex; align-items: center; }
    .btn-cancel { background: none; border: 1px solid rgba(255,255,255,0.1); color: white; padding: 12px 24px; border-radius: 14px; font-weight: 700; cursor: pointer; }

    .stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 24px; margin-bottom: 24px; }
    .stat-card {
      padding: 24px; border-radius: 24px; display: flex; justify-content: space-between; align-items: flex-start;
      .s-label { color: #64748B; font-size: 11px; font-weight: 800; text-transform: uppercase; letter-spacing: 1.5px; }
      .s-val { color: white; font-size: 32px; font-weight: 900; margin: 8px 0; }
      .s-icon { width: 52px; height: 52px; border-radius: 16px; display: flex; align-items: center; justify-content: center; }
      .s-trend { font-size: 11px; font-weight: 700; display: flex; align-items: center; gap: 6px; color: #64748b; }
      .s-trend.up { color: #10b981; }
      .s-trend.down { color: #ef4444; }
    }
    .charts-row { display: grid; grid-template-columns: 2fr 1fr; gap: 24px; margin-bottom: 24px; }
    .chart-card { padding: 28px; border-radius: 28px; display: flex; flex-direction: column; background: rgba(255,255,255,0.01); border: 1px solid rgba(255,255,255,0.04); }
    .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; 
      h3 { color: white; font-size: 16px; font-weight: 800; margin: 0; letter-spacing: -0.5px; }
    }
    .chart-wrapper { position: relative; height: 300px; width: 100%; }
    .funnel-container { display: flex; flex-direction: column; gap: 22px; padding: 10px 0; }
    .status-item { display: flex; justify-content: space-between; align-items: center; color: #cbd5e1; font-weight: 600; font-size: 14px; }
    .s-label-group { display: flex; align-items: center; }
    .status-dot { width: 8px; height: 8px; border-radius: 50%; &.online { background: #10b981; box-shadow: 0 0 12px #10b981; } &.offline { background: #ef4444; box-shadow: 0 0 12px #ef4444; } }
    
    .mini-table-wrapper { overflow-x: auto; }
    .mini-table { width: 100%; border-collapse: collapse; text-align: left; 
      th { color: #64748b; font-size: 10px; font-weight: 800; text-transform: uppercase; letter-spacing: 1px; padding: 12px 16px; border-bottom: 1px solid rgba(255,255,255,0.05); }
      td { padding: 14px 16px; color: #cbd5e1; font-size: 13px; border-bottom: 1px solid rgba(255,255,255,0.02); }
      tr:last-child td { border-bottom: none; }
    }
    .status-pill { padding: 2px 8px; border-radius: 4px; font-size: 10px; font-weight: 800; text-transform: uppercase; &.success { color: #10b981; background: rgba(16, 185, 129, 0.1); } &.pending { color: #f59e0b; background: rgba(245, 158, 11, 0.1); } &.failed { color: #ef4444; background: rgba(239, 68, 68, 0.1); } }

    .badge-gold { background: rgba(225, 202, 150, 0.1); color: var(--accent-gold); padding: 4px 10px; border-radius: 8px; font-size: 10px; font-weight: 800; text-transform: uppercase; border: 1px solid rgba(225, 202, 150, 0.2); }
    .border-t { border-top: 1px solid rgba(255,255,255,0.05); }
    .glass { background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.05); backdrop-filter: blur(10px); }
    .text-gold { color: var(--accent-gold); }
    .fade-in { animation: fadeIn 0.5s ease-out; }
    .fade-in-up { animation: fadeInUp 0.4s cubic-bezier(0.16, 1, 0.3, 1); }
    @keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
    @keyframes fadeInUp { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }

    .pulse-green { width: 10px; height: 10px; border-radius: 50%; background: #10b981; animation: pulse 2s infinite; }
    @keyframes pulse { 0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.7); } 70% { transform: scale(1); box-shadow: 0 0 0 10px rgba(16, 185, 129, 0); } 100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); } }
  `]
})
export class AdminDashboardComponent implements OnInit {
  private http = inject(HttpClient);

  // Real Data Signals
  totalUsers = signal(0);
  totalRecharges = signal(0);
  recentRecharges = signal<RechargeResponse[]>([]);
  totalRevenue = signal(0);
  successCount = signal(0);
  successRate = signal(0);
  operatorCount = signal(0);
  
  // Health Status
  userSvcStatus = signal<'online' | 'offline'>('online');
  rechargeSvcStatus = signal<'online' | 'offline'>('online');
  operatorSvcStatus = signal<'online' | 'offline'>('online');
  
  loading = signal(true);

  // Broadcast Modal State
  exportToCSV() {
    const data = this.recentRecharges();
    if (data.length === 0) {
      this.toast.info('No transactions to export.');
      return;
    }

    const headers = ['Transaction ID', 'Mobile Number', 'Amount', 'Status', 'Timestamp'];
    const csvContent = [
      headers.join(','),
      ...data.map(t => [
        t.id,
        t.mobileNumber,
        t.amount,
        t.status,
        t.createdAt
      ].join(','))
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', 'RechargeHub_Report_' + new Date().toLocaleDateString() + '.csv');
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    this.toast.success('Report exported successfully!');
  }

  showBroadcastModal = signal(false);
  broadcastType = signal('INFO');
  private toast = inject(ToastService);

  ngOnInit() {
    this.fetchPlatformStats();
  }

  fetchPlatformStats() {
    const users$ = this.http.get<any[]>(environment.apiUrl + '/api/users/admin/all');
    const recharges$ = this.http.get<RechargeResponse[]>(environment.apiUrl + '/api/recharges/admin/all');
    const operators$ = this.http.get<any[]>(environment.apiUrl + '/api/operators');

    forkJoin({
      users: users$,
      recharges: recharges$,
      operators: operators$
    }).subscribe({
      next: (data) => {
        this.totalUsers.set(data.users.length);
        this.totalRecharges.set(data.recharges.length);
        this.operatorCount.set(data.operators.length);
        this.loading.set(false);

        const successful = data.recharges.filter(r => r.status === 'SUCCESS');
        this.successCount.set(successful.length);
        
        // Take last 5 for activity feed
        this.recentRecharges.set(data.recharges.slice(0, 5));
        
        const revenue = successful.reduce((acc, curr) => acc + curr.amount, 0);
        this.totalRevenue.set(revenue);

        if (data.recharges.length > 0) {
          const rate = (successful.length / data.recharges.length) * 100;
          this.successRate.set(Math.round(rate * 10) / 10);
        }

        this.updateCharts(data.recharges, data.operators);
        this.updateHealth('online');
      },
      error: (err) => {
        console.error('Dashboard Stats Error:', err);
        this.updateHealth('offline');
        this.loading.set(false);
      }
    });
  }

  sendBroadcast(message: string) {
    if (!message) {
      this.toast.error('Please enter a message');
      return;
    }

    this.http.post(environment.apiUrl + '/api/notifications/broadcast', {
      message: message,
      type: this.broadcastType()
    }).subscribe({
      next: () => {
        this.toast.success('Broadcast dispatched successfully');
        this.showBroadcastModal.set(false);
      },
      error: (err) => {
        console.error(err);
        this.toast.error('Failed to dispatch broadcast');
      }
    });
  }

  private updateHealth(status: 'online' | 'offline') {
    this.userSvcStatus.set(status);
    this.rechargeSvcStatus.set(status);
    this.operatorSvcStatus.set(status);
  }

  // --- Chart Configurations ---

  public lineChartData: ChartConfiguration<'line'>['data'] = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Daily Recharges',
        fill: true,
        tension: 0.4,
        borderColor: '#e1ca96',
        backgroundColor: 'rgba(225, 202, 150, 0.1)',
        pointBackgroundColor: '#e1ca96',
        pointRadius: 4,
        pointHoverRadius: 6
      }
    ]
  };

  public lineChartOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { 
      legend: { display: false },
      tooltip: {
        backgroundColor: 'rgba(18, 20, 22, 0.9)',
        titleColor: '#e1ca96',
        bodyColor: '#fff',
        padding: 12,
        cornerRadius: 8,
        displayColors: false
      }
    },
    scales: {
      x: { grid: { display: false }, ticks: { color: '#64748b', font: { size: 11 } } },
      y: { border: { display: false }, grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#64748b', font: { size: 11 } } }
    }
  };

  public doughnutChartData: ChartConfiguration<'doughnut'>['data'] = {
    labels: ['Success', 'Pending', 'Failed'],
    datasets: [{
      data: [0, 0, 0],
      backgroundColor: ['#10b981', '#f59e0b', '#ef4444'],
      hoverOffset: 15,
      borderWidth: 0
    }]
  };

  public doughnutChartOptions: ChartOptions<'doughnut'> = {
    responsive: true, maintainAspectRatio: false,
    plugins: { 
      legend: { position: 'bottom', labels: { color: '#cbd5e1', padding: 20, font: { size: 12, weight: 'bold' } } } 
    },
    cutout: '75%'
  };

  public barChartData: ChartConfiguration<'bar'>['data'] = {
    labels: [],
    datasets: [
      { 
        data: [], 
        label: 'Revenue (₹)', 
        backgroundColor: 'rgba(225, 202, 150, 0.8)', 
        hoverBackgroundColor: '#e1ca96',
        borderRadius: 8,
        barThickness: 30
      }
    ]
  };

  public barChartOptions: ChartOptions<'bar'> = {
    responsive: true, maintainAspectRatio: false,
    plugins: { 
      legend: { display: false },
      tooltip: { callbacks: { label: (ctx) => ` \u20B9${ctx.raw}` } }
    },
    scales: {
      x: { grid: { display: false }, ticks: { color: '#cbd5e1' } },
      y: { grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#64748b' } }
    }
  };

  private updateCharts(recharges: RechargeResponse[], operators: any[]) {
    // 1. Transaction Status Distribution
    const success = recharges.filter(r => r.status === 'SUCCESS').length;
    const pending = recharges.filter(r => r.status === 'PENDING').length;
    const failed = recharges.filter(r => r.status !== 'SUCCESS' && r.status !== 'PENDING').length;
    this.doughnutChartData.datasets[0].data = [success, pending, failed];

    // 2. 7-Day Trend Analysis
    const last7Days = [...Array(7)].map((_, i) => {
      const d = new Date();
      d.setDate(d.getDate() - (6 - i));
      return d.toLocaleDateString('en-US', { weekday: 'short' });
    });
    
    const trendData = [...Array(7)].map((_, i) => {
      const d = new Date();
      d.setDate(d.getDate() - (6 - i));
      const dateStr = d.toISOString().split('T')[0];
      return recharges.filter(r => r.createdAt.startsWith(dateStr)).length;
    });

    this.lineChartData.labels = last7Days;
    this.lineChartData.datasets[0].data = trendData;

    // 3. Revenue by Operator
    const operatorStats = operators.map(op => {
      const opRevenue = recharges
        .filter(r => r.operatorId === op.id && r.status === 'SUCCESS')
        .reduce((sum, r) => sum + r.amount, 0);
      return { name: op.name, revenue: opRevenue };
    }).sort((a, b) => b.revenue - a.revenue).slice(0, 5);

    this.barChartData.labels = operatorStats.map(s => s.name);
    this.barChartData.datasets[0].data = operatorStats.map(s => s.revenue);

    // Trigger chart updates
    this.lineChartData = { ...this.lineChartData };
    this.doughnutChartData = { ...this.doughnutChartData };
    this.barChartData = { ...this.barChartData };
  }
}
