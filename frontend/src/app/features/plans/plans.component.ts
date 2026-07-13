import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { OperatorService } from '../../core/services/operator.service';
import { Operator, Plan } from '../../core/models/operator.model';
import { LucideAngularModule, Search, Globe, Smartphone, Zap, ZapOff, CheckCircle } from 'lucide-angular';

type Category = 'All' | 'Data' | 'Talktime' | 'Unlimited';

@Component({
  selector: 'app-plans',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, LucideAngularModule],
  template: `
    <div class="plans-container">
      <div class="plans-header">
        <div class="header-content">
          <h1>Browse <span>Plans</span></h1>
          <p>Find the perfect plan for your connection.</p>
        </div>
        
        <div class="search-wrapper glass">
          <lucide-icon name="search" [size]="20"></lucide-icon>
          <input type="text" [(ngModel)]="searchQuery" placeholder="Search by amount or benefits..."/>
        </div>
      </div>

      <!-- Category Filter -->
      <div class="filter-strip">
        <button *ngFor="let cat of categories" 
                [class.active]="activeCategory() === cat"
                (click)="activeCategory.set(cat)">
          <lucide-icon [name]="getCatIcon(cat)" [size]="16"></lucide-icon>
          {{ cat }}
        </button>
      </div>

      <!-- Operators Feed -->
      <div class="operator-sections">
        <div class="op-section fade-in" *ngFor="let op of operators()">
          <ng-container *ngIf="getFilteredPlans(op).length > 0">
            <div class="op-header">
              <div class="op-logo">
                <img [src]="op.logoUrl" *ngIf="op.logoUrl" />
                <span *ngIf="!op.logoUrl">{{ op.name.charAt(0) }}</span>
              </div>
              <div class="op-info">
                <h3>{{ op.name }}</h3>
                <span>{{ op.circle }} • {{ op.type }}</span>
              </div>
              <div class="op-count">{{ getFilteredPlans(op).length }} Available</div>
            </div>

            <div class="plan-grid">
              <div class="plan-card glass hoverable" *ngFor="let plan of getFilteredPlans(op)">
                <div class="p-header">
                  <div class="p-amount">₹{{ plan.amount }}</div>
                  <div class="p-tag" [class]="getPlanCategory(plan).toLowerCase()">{{ getPlanCategory(plan) }}</div>
                </div>
                
                <div class="p-validity">
                  <lucide-icon name="zap" [size]="14"></lucide-icon>
                  Valid for {{ plan.validity }}
                </div>
                
                <p class="p-desc">{{ plan.description }}</p>

                <div class="p-actions">
                  <button class="btn btn-primary btn-sm w-full" routerLink="/recharge">
                    Recharge Now
                  </button>
                </div>
              </div>
            </div>
          </ng-container>
        </div>
      </div>

      <!-- Empty State -->
      <div class="empty-state glass fade-in" *ngIf="operators().length === 0 || isAllEmpty()">
        <lucide-icon name="zap-off" [size]="48"></lucide-icon>
        <h2>No Plans Found</h2>
        <p>Try adjusting your filters or search terms.</p>
      </div>
    </div>
  `,
  styles: [`
    .plans-container { max-width: 1200px; margin: 0 auto; padding: 48px 24px; }
    
    .plans-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 48px; gap: 24px; flex-wrap: wrap; }
    .header-content h1 { font-size: 36px; font-weight: 800; color: white; margin: 0; letter-spacing: -1px; span { color: var(--accent-gold); } }
    .header-content p { color: #94A3B8; font-size: 16px; margin-top: 8px; }

    .search-wrapper {
      display: flex; align-items: center; gap: 12px; padding: 14px 24px; border-radius: 16px; width: 320px;
      lucide-icon { color: var(--accent-gold); opacity: 0.6; }
      input { background: none; border: none; outline: none; color: white; font-weight: 600; width: 100%; &::placeholder { color: #64748B; } }
      &:focus-within { border-color: var(--accent-gold); box-shadow: 0 0 15px rgba(225, 202, 150, 0.1); }
    }

    .filter-strip { display: flex; gap: 12px; margin-bottom: 40px; overflow-x: auto; padding-bottom: 8px;
      button {
        display: flex; align-items: center; gap: 8px; padding: 12px 24px; border-radius: 30px; border: 1px solid rgba(255,255,255,0.05);
        background: rgba(255,255,255,0.02); color: #94A3B8; font-weight: 700; font-size: 13px; cursor: pointer; transition: all 0.3s;
        white-space: nowrap;
        &:hover { background: rgba(255,255,255,0.05); color: white; }
        &.active { background: var(--accent-gold); color: #121416; border-color: var(--accent-gold); box-shadow: var(--glow-gold); }
      }
    }

    .op-section { margin-bottom: 64px; }
    .op-header { 
      display: flex; align-items: center; gap: 20px; margin-bottom: 24px;
      .op-logo { width: 52px; height: 52px; border-radius: 14px; background: #121416; border: 1px solid rgba(255,255,255,0.1); display: flex; align-items: center; justify-content: center; overflow: hidden; img { width: 100%; height: 100%; object-fit: cover; } span { color: var(--accent-gold); font-weight: 800; font-size: 20px; } }
      .op-info h3 { font-size: 20px; font-weight: 800; color: white; margin: 0; }
      .op-info span { color: #64748B; font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 1px; }
      .op-count { margin-left: auto; background: rgba(255,255,255,0.03); padding: 6px 12px; border-radius: 8px; font-size: 11px; font-weight: 800; color: #64748B; }
    }

    .plan-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 24px; }
    .plan-card {
      padding: 24px; border-radius: 24px; display: flex; flex-direction: column; gap: 16px; border: 1px solid rgba(255,255,255,0.05);
      .p-header { display: flex; justify-content: space-between; align-items: center; }
      .p-amount { font-size: 32px; font-weight: 900; color: white; }
      .p-tag { 
        padding: 4px 10px; border-radius: 8px; font-size: 10px; font-weight: 800; text-transform: uppercase;
        &.unlimited { background: rgba(225, 202, 150, 0.1); color: var(--accent-gold); }
        &.data { background: rgba(16, 185, 129, 0.1); color: var(--accent-emerald); }
        &.talktime { background: rgba(59, 130, 246, 0.1); color: #60a5fa; }
        &.all { background: rgba(255,255,255,0.05); color: #94A3B8; }
      }
      .p-validity { display: flex; align-items: center; gap: 8px; font-size: 13px; font-weight: 700; color: var(--accent-gold); lucide-icon { opacity: 0.8; } }
      .p-desc { color: #94A3B8; font-size: 14px; line-height: 1.6; margin: 0; flex: 1; }
      .p-actions { margin-top: 8px; }
    }

    .empty-state { text-align: center; padding: 64px; border-radius: 32px; lucide-icon { color: #64748B; margin-bottom: 24px; } h2 { color: white; } p { color: #64748B; } }

    @keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
    .fade-in { animation: fadeIn 0.5s ease-out forwards; }
  `]
})
export class PlansComponent implements OnInit {
  private operatorSvc = inject(OperatorService);

  operators      = signal<Operator[]>([]);
  activeCategory = signal<Category>('All');
  searchQuery    = '';

  categories: Category[] = ['All', 'Data', 'Talktime', 'Unlimited'];

  ngOnInit(): void {
    this.operatorSvc.getAll().subscribe(ops => this.operators.set(ops));
  }

  getCatIcon(cat: Category): string {
    const icons: any = { All: 'globe', Data: 'smartphone', Talktime: 'zap', Unlimited: 'check-circle' };
    return icons[cat] || 'zap';
  }

  getPlanCategory(plan: Plan): Category {
    const desc = (plan.description || '').toLowerCase();
    if (desc.includes('unlimited')) return 'Unlimited';
    if (desc.includes('data') || desc.includes('gb') || desc.includes('mb')) return 'Data';
    if (desc.includes('call') || desc.includes('talk') || desc.includes('minute')) return 'Talktime';
    return 'All';
  }

  getFilteredPlans(op: Operator): Plan[] {
    const plans = op.plans || [];
    return plans.filter(p => {
      const matchCat = this.activeCategory() === 'All' || this.getPlanCategory(p) === this.activeCategory();
      const q = this.searchQuery.toLowerCase();
      const matchSearch = !q || p.description?.toLowerCase().includes(q) || String(p.amount).includes(q);
      return matchCat && matchSearch;
    });
  }

  isAllEmpty(): boolean {
    return this.operators().every(op => this.getFilteredPlans(op).length === 0);
  }
}
