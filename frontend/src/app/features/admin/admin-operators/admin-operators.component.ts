import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { OperatorService } from '../../../core/services/operator.service';
import { Operator } from '../../../core/models/operator.model';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-admin-operators',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LucideAngularModule],
  template: `
    <div class="fade-in">
      <div class="header-actions">
        <h2>Manage Operators</h2>
        <button class="btn btn-primary" (click)="openModal()">
          <lucide-icon name="plus" [size]="18"></lucide-icon> New Operator
        </button>
      </div>

      <div class="table-container glass mt-4">
        <table class="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Operator Details</th>
              <th>Circle</th>
              <th>Type</th>
              <th>Plans</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngIf="loading()">
              <td colspan="5" class="text-center py-8">
                <lucide-icon name="loader-2" [size]="24" class="spin text-gold mx-auto"></lucide-icon>
              </td>
            </tr>
            <tr *ngIf="!loading() && operators().length === 0">
              <td colspan="5" class="text-center py-8 text-muted">No operators found. Create one above.</td>
            </tr>
            <tr *ngFor="let op of operators()">
              <td class="text-muted">#{{ op.id }}</td>
              <td>
                <div class="op-cell">
                  <div class="op-logo-placeholder" *ngIf="!op.logoUrl">{{ op.name.charAt(0) }}</div>
                  <img [src]="op.logoUrl" *ngIf="op.logoUrl" alt="logo" class="op-logo" (error)="op.logoUrl = ''" />
                  <span class="font-bold text-white">{{ op.name }}</span>
                </div>
              </td>
              <td>{{ op.circle }}</td>
              <td><span class="badge">{{ op.type }}</span></td>
              <td>
                <span class="plan-count">{{ op.plans?.length || 0 }} Plans</span>
              </td>
              <td>
                <div class="action-btns">
                  <button class="btn-icon text-success" (click)="openPlanModal(op)" title="Add Plan">
                    <lucide-icon name="plus-circle" [size]="16"></lucide-icon>
                  </button>
                  <button class="btn-icon" (click)="editOperator(op)" title="Edit">
                    <lucide-icon name="edit-2" [size]="16"></lucide-icon>
                  </button>
                  <button class="btn-icon delete" (click)="deleteOperator(op.id!)" title="Delete">
                    <lucide-icon name="trash-2" [size]="16"></lucide-icon>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Add/Edit Modal (Mocked with CSS) -->
      <div class="modal-backdrop" *ngIf="isModalOpen()">
        <div class="modal glass fade-in-up">
          <div class="modal-header">
            <h3>{{ editingId() ? 'Edit' : 'Create' }} Operator</h3>
            <button class="btn-icon" (click)="closeModal()"><lucide-icon name="x" [size]="20"></lucide-icon></button>
          </div>
          <div class="modal-body">
            <form [formGroup]="opForm" (ngSubmit)="saveOperator()">
              <div class="form-group">
                <label>Operator Name</label>
                <input type="text" class="form-control" formControlName="name" placeholder="e.g. Jio" />
              </div>
              <div class="form-row">
                <div class="form-group flex-1">
                  <label>Circle</label>
                  <input type="text" class="form-control" formControlName="circle" placeholder="e.g. Mumbai" />
                </div>
                <div class="form-group flex-1">
                  <label>Type</label>
                  <select class="form-control" formControlName="type">
                    <option value="Prepaid">Prepaid</option>
                    <option value="Postpaid">Postpaid</option>
                  </select>
                </div>
              </div>
              <div class="form-group">
                <label>Logo URL (Optional)</label>
                <input type="text" class="form-control" formControlName="logoUrl" placeholder="https://..." />
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-ghost" (click)="closeModal()">Cancel</button>
                <button type="submit" class="btn btn-primary" [disabled]="opForm.invalid || saving()">
                  {{ saving() ? 'Saving...' : 'Save Operator' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      <!-- Add Plan Modal -->
      <div class="modal-backdrop" *ngIf="isPlanModalOpen()">
        <div class="modal glass fade-in-up">
          <div class="modal-header">
            <h3>Add Plan for {{ selectedOp()?.name }}</h3>
            <button class="btn-icon" (click)="closePlanModal()"><lucide-icon name="x" [size]="20"></lucide-icon></button>
          </div>
          <div class="modal-body">
            <form [formGroup]="planForm" (ngSubmit)="savePlan()">
              <div class="form-row">
                <div class="form-group flex-1">
                  <label>Amount (₹)</label>
                  <input type="number" class="form-control" formControlName="amount" placeholder="e.g. 199" />
                </div>
                <div class="form-group flex-1">
                  <label>Validity</label>
                  <input type="text" class="form-control" formControlName="validity" placeholder="e.g. 28 Days" />
                </div>
              </div>
              <div class="form-group">
                <label>Data Benefit</label>
                <input type="text" class="form-control" formControlName="data" placeholder="e.g. 1.5 GB/Day" />
              </div>
              <div class="form-group">
                <label>Description</label>
                <textarea class="form-control" formControlName="description" rows="3" placeholder="Plan details..."></textarea>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-ghost" (click)="closePlanModal()">Cancel</button>
                <button type="submit" class="btn btn-primary" [disabled]="planForm.invalid || saving()">
                  {{ saving() ? 'Saving...' : 'Add Plan' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .header-actions { display: flex; justify-content: space-between; align-items: center; h2 { margin: 0; color: white; } }
    .glass { background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.05); }
    .table-container { border-radius: 20px; overflow: hidden; }
    .admin-table { width: 100%; border-collapse: collapse; text-align: left; }
    .admin-table th { padding: 20px; background: rgba(255,255,255,0.03); color: #cbd5e1; font-weight: 600; font-size: 13px; text-transform: uppercase; letter-spacing: 1px; }
    .admin-table td { padding: 16px 20px; color: #94a3b8; border-bottom: 1px solid rgba(255,255,255,0.03); font-size: 14px; }
    .admin-table tr:last-child td { border-bottom: none; }
    
    .op-cell { display: flex; align-items: center; gap: 12px; }
    .op-logo { width: 32px; height: 32px; border-radius: 8px; object-fit: cover; }
    .op-logo-placeholder { width: 32px; height: 32px; border-radius: 8px; background: var(--accent-gold); color: #121416; display: flex; align-items: center; justify-content: center; font-weight: bold; }
    .badge { padding: 4px 10px; background: rgba(255,255,255,0.05); border-radius: 6px; font-size: 11px; font-weight: 600; color: #e1ca96; }
    .plan-count { font-size: 12px; color: #94a3b8; font-weight: 600; }
    .text-success { color: #10b981 !important; }
    
    .action-btns { display: flex; gap: 8px; }
    .btn-icon { background: none; border: none; color: #64748b; cursor: pointer; padding: 6px; border-radius: 6px; transition: all 0.2s; &:hover { background: rgba(255,255,255,0.05); color: white; } &.delete:hover { color: #ef4444; background: rgba(239,68,68,0.1); } }
    
    /* Modal */
    .modal-backdrop { position: fixed; inset: 0; background: rgba(0,0,0,0.6); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 100; }
    .modal { width: 100%; max-width: 500px; border-radius: 20px; overflow: hidden; background: #121416; box-shadow: 0 25px 50px -12px rgba(0,0,0,0.5); }
    .modal-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 24px; border-bottom: 1px solid rgba(255,255,255,0.05); h3 { margin: 0; color: white; } }
    .modal-body { padding: 24px; }
    .modal-footer { display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px; padding-top: 24px; border-top: 1px solid rgba(255,255,255,0.05); }
    
    /* Forms */
    .form-group { margin-bottom: 16px; label { display: block; color: #cbd5e1; font-size: 13px; margin-bottom: 8px; } }
    .form-row { display: flex; gap: 16px; }
    .flex-1 { flex: 1; }
    .form-control { width: 100%; padding: 12px 16px; background: rgba(0,0,0,0.2); border: 1px solid rgba(255,255,255,0.1); border-radius: 12px; color: white; font-size: 14px; outline: none; transition: border-color 0.2s; &:focus { border-color: var(--accent-gold); } }
    select.form-control { appearance: none; background-image: url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23cbd5e1' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3e%3cpolyline points='6 9 12 15 18 9'%3e%3c/polyline%3e%3c/svg%3e"); background-repeat: no-repeat; background-position: right 1rem center; background-size: 1em; }
    
    .spin { animation: spin 1s linear infinite; }
    @keyframes spin { 100% { transform: rotate(360deg); } }
    .fade-in { animation: fadeIn 0.3s ease-out; }
    .fade-in-up { animation: fadeInUp 0.3s cubic-bezier(0.16, 1, 0.3, 1); }
    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
    @keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
  `]
})
export class AdminOperatorsComponent implements OnInit {
  private operatorSvc = inject(OperatorService);
  private toast = inject(ToastService);
  private fb = inject(FormBuilder);

  operators = signal<Operator[]>([]);
  loading = signal(true);
  
  // Modal State
  isModalOpen = signal(false);
  saving = signal(false);
  editingId = signal<number | null>(null);

  opForm = this.fb.group({
    name: ['', Validators.required],
    circle: ['', Validators.required],
    type: ['Prepaid', Validators.required],
    logoUrl: ['']
  });

  // Plan Form
  isPlanModalOpen = signal(false);
  selectedOp = signal<Operator | null>(null);
  planForm = this.fb.group({
    amount: [0, [Validators.required, Validators.min(1)]],
    validity: ['', Validators.required],
    description: ['', Validators.required],
    data: [''],
    type: ['Prepaid', Validators.required]
  });

  ngOnInit() {
    this.fetchOperators();
  }

  fetchOperators() {
    this.loading.set(true);
    this.operatorSvc.getAll().subscribe({
      next: (res) => { this.operators.set(res); this.loading.set(false); },
      error: () => { this.toast.error('Failed to load operators'); this.loading.set(false); }
    });
  }

  openModal() {
    this.editingId.set(null);
    this.opForm.reset({ type: 'Prepaid' });
    this.isModalOpen.set(true);
  }

  editOperator(op: Operator) {
    this.editingId.set(op.id!);
    this.opForm.patchValue({
      name: op.name,
      circle: op.circle,
      type: op.type,
      logoUrl: op.logoUrl
    });
    this.isModalOpen.set(true);
  }

  closeModal() {
    this.isModalOpen.set(false);
  }

  saveOperator() {
    if (this.opForm.invalid) return;
    this.saving.set(true);
    
    const val = this.opForm.value as Operator;
    const req = this.editingId() 
      ? this.operatorSvc.update(this.editingId()!, val)
      : this.operatorSvc.create(val);

    req.subscribe({
      next: () => {
        this.toast.success(`Operator ${this.editingId() ? 'updated' : 'created'} successfully`);
        this.fetchOperators();
        this.closeModal();
        this.saving.set(false);
      },
      error: () => {
        this.toast.error('Failed to save operator');
        this.saving.set(false);
      }
    });
  }

  // Plan Methods
  openPlanModal(op: Operator) {
    this.selectedOp.set(op);
    this.planForm.reset({ type: op.type, amount: 0 });
    this.isPlanModalOpen.set(true);
  }

  closePlanModal() {
    this.isPlanModalOpen.set(false);
  }

  savePlan() {
    if (this.planForm.invalid || !this.selectedOp()) return;
    this.saving.set(true);
    
    const val = this.planForm.value;
    this.operatorSvc.createPlan(this.selectedOp()!.id!, val as any).subscribe({
      next: () => {
        this.toast.success('Plan added successfully');
        this.fetchOperators();
        this.closePlanModal();
        this.saving.set(false);
      },
      error: () => {
        this.toast.error('Failed to add plan');
        this.saving.set(false);
      }
    });
  }

  deleteOperator(id: number) {
    if (confirm('Are you sure you want to delete this operator? All associated plans will also be deleted.')) {
      this.operatorSvc.delete(id).subscribe({
        next: () => {
          this.toast.success('Operator deleted');
          this.fetchOperators();
        },
        error: () => this.toast.error('Failed to delete operator')
      });
    }
  }
}
