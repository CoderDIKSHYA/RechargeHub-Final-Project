import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { ToastService } from '../../../core/services/toast.service';
import { LucideAngularModule } from 'lucide-angular';
import { RechargeResponse } from '../../../core/models/recharge.model';

@Component({
  selector: 'app-admin-history',
  standalone: true,
  imports: [CommonModule, LucideAngularModule, DatePipe],
  template: `
    <div class="fade-in">
      <div class="header-actions">
        <h2>Platform Transactions</h2>
        <button class="btn btn-ghost" (click)="downloadCsv()">
          <lucide-icon name="download" [size]="18"></lucide-icon> Download CSV
        </button>
      </div>

      <!-- Filters -->
      <div class="filters glass mt-6">
        <div class="search-box">
          <lucide-icon name="search" [size]="18" class="text-muted"></lucide-icon>
          <input type="text" placeholder="Search Mobile / Txn ID..." (input)="onSearch($event)" />
        </div>
        <div class="select-wrapper">
          <lucide-icon name="filter" [size]="16" class="filter-icon"></lucide-icon>
          <select (change)="onStatusFilter($event)">
            <option value="ALL">All Statuses</option>
            <option value="SUCCESS">Success</option>
            <option value="PENDING">Pending</option>
            <option value="FAILED">Failed</option>
          </select>
          <lucide-icon name="chevron-down" [size]="16" class="chevron-icon"></lucide-icon>
        </div>
      </div>

      <div class="table-container glass mt-4 shadow-2xl">
        <table class="admin-table">
          <thead>
            <tr>
              <th>TXN ID</th>
              <th>DATE & TIME</th>
              <th>USER ID</th>
              <th>MOBILE</th>
              <th>AMOUNT</th>
              <th>STATUS</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngIf="loading()">
              <td colspan="6" class="text-center py-16">
                <lucide-icon name="loader-2" [size]="32" class="spin text-gold mx-auto"></lucide-icon>
              </td>
            </tr>
            <tr *ngIf="!loading() && filteredRecharges().length === 0">
              <td colspan="6" class="text-center py-16 text-muted">No transactions found matching your criteria.</td>
            </tr>
            <tr *ngFor="let r of filteredRecharges()" class="txn-row">
              <td class="font-mono text-sm font-bold text-white">{{ r.transactionId || 'N/A' }}</td>
              <td>
                <div class="date-cell">
                  <span class="text-white">{{ r.createdAt | date:'MMM dd, yyyy' }}</span>
                  <span class="time">{{ r.createdAt | date:'hh:mm a' }}</span>
                </div>
              </td>
              <td class="text-muted">#USR-{{ r.userId }}</td>
              <td class="font-bold text-white">{{ r.mobileNumber }}</td>
              <td class="text-gold font-bold">₹{{ r.amount }}</td>
              <td>
                <span class="badge" [ngClass]="{
                  'success': r.status === 'SUCCESS',
                  'pending': r.status === 'PENDING',
                  'failed': r.status === 'FAILED' || r.status === 'FAIL'
                }">
                  {{ r.status }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .header-actions { display: flex; justify-content: space-between; align-items: center; h2 { margin: 0; color: white; font-weight: 800; font-size: 24px; } }
    
    .filters { padding: 8px; border-radius: 20px; display: flex; gap: 12px; margin-bottom: 24px; background: rgba(255,255,255,0.02); }
    
    .search-box { 
      flex: 1; display: flex; align-items: center; gap: 12px; background: rgba(0,0,0,0.2); padding: 12px 20px; border-radius: 16px; border: 1px solid rgba(255,255,255,0.05);
      input { flex: 1; background: none; border: none; color: white; outline: none; font-size: 14px; &::placeholder { color: #64748b; } }
    }

    .select-wrapper {
      position: relative; min-width: 180px; display: flex; align-items: center; background: rgba(0,0,0,0.2); border-radius: 16px; border: 1px solid rgba(255,255,255,0.05); padding: 0 16px;
      .filter-icon { color: #64748b; margin-right: 8px; }
      .chevron-icon { position: absolute; right: 16px; color: #64748b; pointer-events: none; }
      select { 
        width: 100%; height: 48px; background: none; border: none; color: white; outline: none; font-size: 14px; cursor: pointer; appearance: none; padding-right: 30px;
        option { background: #121416; color: white; padding: 10px; }
      }
    }

    .glass { background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.05); backdrop-filter: blur(10px); }
    .table-container { border-radius: 24px; overflow: visible; /* Changed to visible to prevent clipping */ }
    .admin-table { width: 100%; border-collapse: collapse; text-align: left; }
    .admin-table th { padding: 20px 24px; background: rgba(255,255,255,0.03); color: #94a3b8; font-weight: 700; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; border-bottom: 1px solid rgba(255,255,255,0.05); }
    .admin-table td { padding: 18px 24px; color: #cbd5e1; border-bottom: 1px solid rgba(255,255,255,0.03); font-size: 14px; }
    .txn-row:hover td { background: rgba(255,255,255,0.02); }
    .admin-table tr:last-child td { border-bottom: none; }
    
    .date-cell { display: flex; flex-direction: column; gap: 2px; .time { font-size: 11px; color: #64748b; font-weight: 600; } }
    .font-mono { font-family: 'JetBrains Mono', monospace; }
    
    .badge { padding: 6px 12px; border-radius: 8px; font-size: 10px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.5px; }
    .badge.success { background: rgba(16, 185, 129, 0.1); color: #10b981; border: 1px solid rgba(16, 185, 129, 0.2); }
    .badge.pending { background: rgba(245, 158, 11, 0.1); color: #f59e0b; border: 1px solid rgba(245, 158, 11, 0.2); }
    .badge.failed { background: rgba(239, 68, 68, 0.1); color: #ef4444; border: 1px solid rgba(239, 68, 68, 0.2); }
    
    .btn-ghost { background: none; border: none; color: #cbd5e1; cursor: pointer; padding: 10px 20px; border-radius: 12px; font-weight: 700; font-size: 13px; display: flex; align-items: center; gap: 8px; transition: all 0.2s; border: 1px solid rgba(255,255,255,0.1); &:hover { color: white; background: rgba(255,255,255,0.05); border-color: rgba(255,255,255,0.2); } }
    
    .text-gold { color: var(--accent-gold); }
    .spin { animation: spin 1s linear infinite; }
    @keyframes spin { 100% { transform: rotate(360deg); } }
    .fade-in { animation: fadeIn 0.4s ease-out; }
    @keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
  `]
})
export class AdminHistoryComponent implements OnInit {
  private http = inject(HttpClient);
  private toast = inject(ToastService);
  
  allRecharges = signal<RechargeResponse[]>([]);
  filteredRecharges = signal<RechargeResponse[]>([]);
  loading = signal(true);

  searchTerm = '';
  statusFilter = 'ALL';

  ngOnInit() {
    this.fetchHistory();
  }

  fetchHistory() {
    this.loading.set(true);
    this.http.get<RechargeResponse[]>(`${environment.apiUrl}/api/recharges/admin/all`).subscribe({
      next: (res) => {
        // Sort by newest first
        res.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
        this.allRecharges.set(res);
        this.applyFilters();
        this.loading.set(false);
      },
      error: (err) => {
        console.error(err);
        this.toast.error('Failed to load platform transactions');
        this.loading.set(false);
      }
    });
  }

  onSearch(event: any) {
    this.searchTerm = event.target.value.toLowerCase();
    this.applyFilters();
  }

  onStatusFilter(event: any) {
    this.statusFilter = event.target.value;
    this.applyFilters();
  }

  applyFilters() {
    let filtered = this.allRecharges();
    
    if (this.statusFilter !== 'ALL') {
      filtered = filtered.filter(r => r.status.toUpperCase() === this.statusFilter);
    }
    
    if (this.searchTerm) {
      filtered = filtered.filter(r => 
        (r.transactionId && r.transactionId.toLowerCase().includes(this.searchTerm)) ||
        (r.mobileNumber && r.mobileNumber.includes(this.searchTerm))
      );
    }
    
    this.filteredRecharges.set(filtered);
  }

  downloadCsv() {
    // Dummy action
    this.toast.info('Preparing CSV download... (Simulated)');
  }
}
