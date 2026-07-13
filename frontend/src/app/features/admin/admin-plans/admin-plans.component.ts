import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { OperatorService } from '../../../core/services/operator.service';
import { Operator, Plan } from '../../../core/models/operator.model';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-admin-plans',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LucideAngularModule],
  template: `
    <div class="fade-in">
      <div class="header-actions">
        <h2>Manage Plans</h2>
        
        <div class="operator-selector-card glass">
          <div class="selector-info">
            <lucide-icon name="smartphone" [size]="20" class="text-gold"></lucide-icon>
            <div>
              <span class="label">Filter by Operator</span>
              <p class="desc">Select to manage plans</p>
            </div>
          </div>
          <div class="select-wrapper">
            <select class="form-control" (change)="onOperatorChange($event)">
              <option value="">-- Choose Operator --</option>
              <option *ngFor="let op of operators()" [value]="op.id">{{ op.name }} ({{ op.circle }})</option>
            </select>
            <lucide-icon name="chevron-down" [size]="18" class="chevron-icon"></lucide-icon>
          </div>
        </div>
      </div>

      <div class="mt-8" *ngIf="selectedOperatorId()">
        <div class="table-actions mb-4">
          <button class="btn btn-primary shadow-lg" (click)="openModal()">
            <lucide-icon name="plus" [size]="18"></lucide-icon> Add New Plan
          </button>
        </div>

        <div class="table-container glass shadow-2xl">
          <table class="admin-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Price</th>
                <th>Validity</th>
                <th>Data</th>
                <th>Description</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngIf="selectedOperator()?.plans?.length === 0">
                <td colspan="6" class="text-center py-8 text-muted">No plans found for this operator.</td>
              </tr>
              <tr *ngFor="let plan of selectedOperator()?.plans">
                <td class="text-muted">#{{ plan.id }}</td>
                <td class="font-bold text-gold">₹{{ plan.amount }}</td>
                <td>{{ plan.validity }}</td>
                <td>{{ plan.data }}</td>
                <td class="desc-cell" [title]="plan.description">{{ plan.description }}</td>
                <td>
                  <div class="action-btns">
                    <button class="btn-icon" (click)="editPlan(plan)" title="Edit">
                      <lucide-icon name="edit-2" [size]="16"></lucide-icon>
                    </button>
                    <button class="btn-icon delete" (click)="deletePlan(plan.id!)" title="Delete">
                      <lucide-icon name="trash-2" [size]="16"></lucide-icon>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      
      <div class="mt-12 text-center text-muted" *ngIf="!selectedOperatorId()">
        <lucide-icon name="credit-card" [size]="48" class="mx-auto mb-4 opacity-50"></lucide-icon>
        <p>Please select an operator to view and manage plans.</p>
      </div>

      <!-- Add/Edit Modal -->
      <div class="modal-backdrop" *ngIf="isModalOpen()">
        <div class="modal glass fade-in-up">
          <div class="modal-header">
            <h3>{{ editingId() ? 'Edit' : 'Create' }} Plan</h3>
            <button class="btn-icon" (click)="closeModal()"><lucide-icon name="x" [size]="20"></lucide-icon></button>
          </div>
          <div class="modal-body">
            <form [formGroup]="planForm" (ngSubmit)="savePlan()">
              <div class="form-row">
                <div class="form-group flex-1">
                  <label>Amount (₹)</label>
                  <input type="number" class="form-control" formControlName="amount" placeholder="e.g. 299" />
                </div>
                <div class="form-group flex-1">
                  <label>Validity (Days)</label>
                  <input type="text" class="form-control" formControlName="validity" placeholder="e.g. 28 Days" />
                </div>
              </div>
              <div class="form-row">
                <div class="form-group flex-1">
                  <label>Data Limit</label>
                  <input type="text" class="form-control" formControlName="data" placeholder="e.g. 2GB/Day" />
                </div>
                <div class="form-group flex-1">
                  <label>Plan Type</label>
                  <select class="form-control" formControlName="type">
                    <option value="Popular">Popular</option>
                    <option value="Data">Data</option>
                    <option value="Validity">Validity</option>
                    <option value="Unlimited">Unlimited</option>
                  </select>
                </div>
              </div>
              <div class="form-group">
                <label>Description</label>
                <textarea class="form-control" formControlName="description" rows="3" placeholder="Plan details..."></textarea>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-ghost" (click)="closeModal()">Cancel</button>
                <button type="submit" class="btn btn-primary" [disabled]="planForm.invalid || saving()">
                  {{ saving() ? 'Saving...' : 'Save Plan' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .header-actions { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; h2 { margin: 0; color: white; font-weight: 800; font-size: 24px; } }
    
    .operator-selector-card { 
      display: flex; align-items: center; gap: 24px; padding: 12px 24px; border-radius: 20px; background: rgba(255,255,255,0.03);
      .selector-info { display: flex; align-items: center; gap: 16px; border-right: 1px solid rgba(255,255,255,0.05); padding-right: 24px; }
      .label { display: block; font-size: 11px; font-weight: 800; text-transform: uppercase; letter-spacing: 1px; color: #64748b; }
      .desc { margin: 0; font-size: 13px; color: #cbd5e1; font-weight: 600; }
    }

    .select-wrapper {
      position: relative; min-width: 280px; display: flex; align-items: center;
      .chevron-icon { position: absolute; right: 16px; color: #64748b; pointer-events: none; }
      select { 
        width: 100%; height: 50px; background: rgba(0,0,0,0.3); border: 1px solid rgba(255,255,255,0.1); border-radius: 14px; color: white; padding: 0 16px; font-size: 14px; font-weight: 600; cursor: pointer; appearance: none; transition: all 0.2s;
        &:focus { border-color: var(--accent-gold); box-shadow: 0 0 0 4px rgba(225, 202, 150, 0.1); }
        option { background: #121416; color: white; padding: 12px; }
      }
    }
    
    .glass { background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.05); backdrop-filter: blur(10px); }
    .table-container { border-radius: 24px; overflow: visible; }
    .admin-table { width: 100%; border-collapse: collapse; text-align: left; }
    .admin-table th { padding: 20px 24px; background: rgba(255,255,255,0.03); color: #94a3b8; font-weight: 700; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; border-bottom: 1px solid rgba(255,255,255,0.05); }
    .admin-table td { padding: 16px 24px; color: #cbd5e1; border-bottom: 1px solid rgba(255,255,255,0.03); font-size: 14px; }
    .admin-table tr:last-child td { border-bottom: none; }
    .desc-cell { max-width: 250px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    
    .text-gold { color: var(--accent-gold); }
    
    .action-btns { display: flex; gap: 8px; }
    .btn-icon { background: none; border: none; color: #64748b; cursor: pointer; padding: 6px; border-radius: 6px; transition: all 0.2s; &:hover { background: rgba(255,255,255,0.05); color: white; } &.delete:hover { color: #ef4444; background: rgba(239,68,68,0.1); } }
    
    /* Modal */
    .modal-backdrop { position: fixed; inset: 0; background: rgba(0,0,0,0.6); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 100; }
    .modal { width: 100%; max-width: 550px; border-radius: 20px; overflow: hidden; background: #121416; box-shadow: 0 25px 50px -12px rgba(0,0,0,0.5); }
    .modal-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 24px; border-bottom: 1px solid rgba(255,255,255,0.05); h3 { margin: 0; color: white; } }
    .modal-body { padding: 24px; }
    .modal-footer { display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px; padding-top: 24px; border-top: 1px solid rgba(255,255,255,0.05); }
    
    /* Forms */
    .form-group { margin-bottom: 16px; label { display: block; color: #cbd5e1; font-size: 13px; margin-bottom: 8px; } }
    .form-row { display: flex; gap: 16px; }
    .flex-1 { flex: 1; }
    .form-control { width: 100%; padding: 12px 16px; background: rgba(0,0,0,0.2); border: 1px solid rgba(255,255,255,0.1); border-radius: 12px; color: white; font-size: 14px; outline: none; transition: border-color 0.2s; &:focus { border-color: var(--accent-gold); } }
    select.form-control { appearance: none; background-image: url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23cbd5e1' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3e%3cpolyline points='6 9 12 15 18 9'%3e%3c/polyline%3e%3c/svg%3e"); background-repeat: no-repeat; background-position: right 1rem center; background-size: 1em; }
    textarea.form-control { resize: vertical; min-height: 80px; }
    
    .fade-in { animation: fadeIn 0.3s ease-out; }
    .fade-in-up { animation: fadeInUp 0.3s cubic-bezier(0.16, 1, 0.3, 1); }
    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
    @keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
  `]
})
export class AdminPlansComponent implements OnInit {
  private operatorSvc = inject(OperatorService);
  private toast = inject(ToastService);
  private fb = inject(FormBuilder);

  operators = signal<Operator[]>([]);
  selectedOperatorId = signal<number | null>(null);
  selectedOperator = signal<Operator | null>(null);
  
  // Modal State
  isModalOpen = signal(false);
  saving = signal(false);
  editingId = signal<number | null>(null);

  planForm = this.fb.group({
    amount: ['', [Validators.required, Validators.min(1)]],
    validity: ['', Validators.required],
    data: ['', Validators.required],
    type: ['Popular', Validators.required],
    description: ['', Validators.required]
  });

  ngOnInit() {
    this.fetchOperators();
  }

  fetchOperators() {
    this.operatorSvc.getAll().subscribe(res => {
      this.operators.set(res);
      if (this.selectedOperatorId()) {
        const op = res.find(o => o.id === this.selectedOperatorId());
        this.selectedOperator.set(op || null);
      }
    });
  }

  onOperatorChange(event: any) {
    const id = event.target.value;
    if (id) {
      this.selectedOperatorId.set(Number(id));
      const op = this.operators().find(o => o.id === Number(id));
      this.selectedOperator.set(op || null);
    } else {
      this.selectedOperatorId.set(null);
      this.selectedOperator.set(null);
    }
  }

  openModal() {
    this.editingId.set(null);
    this.planForm.reset({ type: 'Popular' });
    this.isModalOpen.set(true);
  }

  editPlan(plan: Plan) {
    this.editingId.set(plan.id!);
    this.planForm.patchValue({
      amount: plan.amount as any,
      validity: plan.validity,
      data: plan.data,
      type: plan.type,
      description: plan.description
    });
    this.isModalOpen.set(true);
  }

  closeModal() {
    this.isModalOpen.set(false);
  }

  savePlan() {
    if (this.planForm.invalid || !this.selectedOperatorId()) return;
    this.saving.set(true);
    
    const val = this.planForm.value as unknown as Plan;
    val.operatorId = this.selectedOperatorId()!;

    const req = this.editingId() 
      ? this.operatorSvc.updatePlan(this.editingId()!, val)
      : this.operatorSvc.createPlan(this.selectedOperatorId()!, val);

    req.subscribe({
      next: () => {
        this.toast.success(`Plan ${this.editingId() ? 'updated' : 'created'} successfully`);
        this.fetchOperators(); // Refresh to get updated plans
        this.closeModal();
        this.saving.set(false);
      },
      error: () => {
        this.toast.error('Failed to save plan');
        this.saving.set(false);
      }
    });
  }

  deletePlan(id: number) {
    if (confirm('Are you sure you want to delete this plan?')) {
      this.operatorSvc.deletePlan(id).subscribe({
        next: () => {
          this.toast.success('Plan deleted');
          this.fetchOperators();
        },
        error: () => this.toast.error('Failed to delete plan')
      });
    }
  }
}
