import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, LucideAngularModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="admin-layout">
      <!-- Sidebar -->
      <aside class="admin-sidebar">
        <div class="sidebar-brand">
          <div class="brand-icon"><lucide-icon name="zap" [size]="20"></lucide-icon></div>
          <span>RechargeHub <span>Admin</span></span>
        </div>

        <nav class="sidebar-nav">
          <a *ngFor="let item of navItems" 
             [routerLink]="item.path"
             routerLinkActive="active"
             class="nav-item">
            <lucide-icon [name]="item.icon" [size]="18"></lucide-icon>
            {{ item.label }}
          </a>
        </nav>

        <div class="sidebar-footer">
          <button (click)="logout()" class="logout-btn">
            <lucide-icon name="log-out" [size]="18"></lucide-icon>
            Sign Out
          </button>
        </div>
      </aside>

      <!-- Main Content -->
      <main class="admin-main">
        <header class="admin-header">
          <div class="header-left">
            <h1>Platform Oversight</h1>
            <p>Management & Analytics</p>
          </div>
          <div class="header-right">
            <div class="admin-profile">
              <div class="avatar">A</div>
              <span>Administrator</span>
            </div>
          </div>
        </header>

        <!-- Dynamic View -->
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styles: [`
    .admin-layout { display: flex; min-height: 100vh; background: #03010A; font-family: 'Inter', sans-serif; }
    
    /* Sidebar */
    .admin-sidebar {
      width: 280px; background: rgba(255,255,255,0.02); border-right: 1px solid rgba(255,255,255,0.05);
      display: flex; flex-direction: column; padding: 32px;
    }
    .sidebar-brand {
      display: flex; align-items: center; gap: 12px; margin-bottom: 48px;
      .brand-icon { width: 32px; height: 32px; background: rgba(225, 202, 150, 0.1); border-radius: 8px; display: flex; align-items: center; justify-content: center; color: var(--accent-gold); }
      span { font-size: 18px; font-weight: 900; color: white; span { color: var(--accent-gold); } }
    }
    .sidebar-nav {
      flex: 1; display: flex; flex-direction: column; gap: 8px;
      .nav-item {
        display: flex; align-items: center; gap: 12px; padding: 14px 20px; border: none; background: none;
        color: #64748B; font-weight: 700; font-size: 14px; border-radius: 12px; cursor: pointer; transition: all 0.3s; text-decoration: none;
        lucide-icon { opacity: 0.6; }
        &:hover { background: rgba(255,255,255,0.03); color: white; }
        &.active { background: rgba(225, 202, 150, 0.1); color: var(--accent-gold); lucide-icon { opacity: 1; } }
      }
    }
    .logout-btn {
      display: flex; align-items: center; gap: 12px; padding: 14px 20px; border: none; background: none;
      color: #ef4444; font-weight: 700; font-size: 14px; cursor: pointer; margin-top: auto;
      transition: all 0.3s ease;
      &:hover { background: rgba(239, 68, 68, 0.1); border-radius: 12px; }
    }

    /* Main Area */
    .admin-main { flex: 1; padding: 48px 64px; height: 100vh; overflow-y: auto; }
    .admin-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 48px;
      h1 { font-size: 32px; font-weight: 900; color: white; margin: 0 0 8px; }
      p { color: #64748B; font-size: 14px; font-weight: 600; margin: 0; text-transform: uppercase; letter-spacing: 1px; }
    }
    .admin-profile { display: flex; align-items: center; gap: 12px; 
      .avatar { width: 40px; height: 40px; border-radius: 12px; background: var(--accent-gold); color: #121416; display: flex; align-items: center; justify-content: center; font-weight: 800; font-size: 18px; }
      span { color: white; font-weight: 700; font-size: 14px; }
    }
  `]
})
export class AdminComponent {
  private authSvc = inject(AuthService);
  private router  = inject(Router);

  navItems = [
    { path: '/admin/dashboard', label: 'Analytics Hub', icon: 'layout-dashboard' },
    { path: '/admin/operators', label: 'Operators',     icon: 'radio' },
    { path: '/admin/plans',     label: 'Plans',         icon: 'credit-card' },
    { path: '/admin/users',     label: 'Users',         icon: 'users' },
    { path: '/admin/history',   label: 'Transactions',  icon: 'history' }
  ];

  logout() {
    this.authSvc.logout();
    this.router.navigate(['/admin/login']);
  }
}
