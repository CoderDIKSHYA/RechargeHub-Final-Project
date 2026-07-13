import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { RechargeService } from '../../core/services/recharge.service';
import { RechargeResponse } from '../../core/models/recharge.model';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page">
      <div class="page-header">
        <h2>📋 Recharge History</h2>
        <p>All your past recharge transactions</p>
      </div>

      <div class="card">
        <div class="empty-state" *ngIf="recharges().length === 0 && !loading()">
          <div class="icon">📋</div>
          <p>No recharge history found.</p>
        </div>

        <div class="loading" *ngIf="loading()">Loading...</div>

        <table *ngIf="recharges().length > 0">
          <thead>
            <tr>
              <th>#</th>
              <th>Mobile</th>
              <th>Operator</th>
              <th>Amount</th>
              <th>Status</th>
              <th>Date</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let r of recharges()">
              <td>{{ r.id }}</td>
              <td>{{ r.mobileNumber }}</td>
              <td>{{ r.operatorId }}</td>
              <td><strong class="text-emerald">₹{{ r.amount }}</strong></td>
              <td>
                <span class="badge" [class]="r.status.toLowerCase()">{{ r.status }}</span>
              </td>
              <td>{{ r.createdAt | date:'dd MMM yyyy, hh:mm a' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .page { padding: 24px; max-width: 1000px; margin: 0 auto; }
    .page-header { margin-bottom: 24px; h2 { font-size: 22px; font-weight: 700; color: var(--text-gold); } p { color: var(--text-muted); font-size: 14px; margin-top: 4px; } }
    .loading { text-align: center; padding: 40px; color: var(--text-muted); }
  `]
})
export class HistoryComponent implements OnInit {
  recharges = signal<RechargeResponse[]>([]);
  loading   = signal(true);

  constructor(private auth: AuthService, private rechargeSvc: RechargeService) {}

  ngOnInit(): void {
    const userId = this.auth.currentUser()?.id;
    if (!userId) return;

    this.rechargeSvc.getByUserId(userId).subscribe({
      next: (data) => { this.recharges.set(data); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }
}
