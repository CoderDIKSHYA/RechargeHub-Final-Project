import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ToastComponent } from '../toast/toast.component';
import { LucideAngularModule, LayoutDashboard, Zap, BookOpen, Clock, Bell, User, Shield, LogOut, ChevronRight, Moon, Sun } from 'lucide-angular';
import { ThemeService } from '../../core/services/theme.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet, ToastComponent, LucideAngularModule],
  template: `
    <div class="app-shell">
      <!-- Sidebar -->
      <aside class="sidebar">
        <div class="sidebar-logo">
          <div class="logo-container">
            <lucide-icon name="zap" [size]="24" class="logo-bolt"></lucide-icon>
          </div>
          <span class="logo-text">Recharge<span>Hub</span></span>
        </div>

        <nav class="sidebar-nav">
          <div class="nav-section-title">Main Menu</div>
          <a routerLink="/dashboard"     routerLinkActive="active" class="nav-item">
            <lucide-icon name="layout-dashboard" [size]="20"></lucide-icon> Dashboard
          </a>
          <a routerLink="/recharge"      routerLinkActive="active" class="nav-item">
            <lucide-icon name="zap" [size]="20"></lucide-icon> Recharge
          </a>
          <a routerLink="/plans"         routerLinkActive="active" class="nav-item">
            <lucide-icon name="book-open" [size]="20"></lucide-icon> Plans
          </a>

          <div class="nav-section-title">Account</div>
          <a routerLink="/history"       routerLinkActive="active" class="nav-item">
            <lucide-icon name="clock" [size]="20"></lucide-icon> History
          </a>
          <a routerLink="/notifications" routerLinkActive="active" class="nav-item">
            <lucide-icon name="bell" [size]="20"></lucide-icon> Notifications
          </a>
          <a routerLink="/profile"       routerLinkActive="active" class="nav-item">
            <lucide-icon name="user" [size]="20"></lucide-icon> Profile
          </a>

          <div class="nav-section-title" *ngIf="isAdmin()">Administration</div>
          <a routerLink="/admin" routerLinkActive="active" class="nav-item admin-link" *ngIf="isAdmin()">
            <lucide-icon name="shield" [size]="20"></lucide-icon> Admin Panel
          </a>
        </nav>

        <div class="sidebar-footer">
          <div class="user-card">
            <div class="user-avatar">{{ user()?.name?.charAt(0)?.toUpperCase() }}</div>
            <div class="user-meta">
              <span class="u-name">{{ user()?.name }}</span>
              <span class="u-role">{{ user()?.role === 'ROLE_ADMIN' ? 'Administrator' : 'Premium Member' }}</span>
            </div>
            <button class="logout-btn" (click)="auth.logout()" title="Logout">
              <lucide-icon name="log-out" [size]="18"></lucide-icon>
            </button>
          </div>
        </div>
      </aside>

      <!-- Main Content -->
      <main class="main-content">
        <router-outlet/>
      </main>
    </div>

    <!-- Global Toast Notifications -->
    <app-toast/>
  `,
  styles: [`
    .app-shell { display: flex; min-height: 100vh; background: var(--bg-main); }

    .sidebar {
      width: 280px; flex-shrink: 0;
      background: var(--bg-sidebar);
      border-right: 1px solid var(--glass-border);
      display: flex; flex-direction: column;
      position: sticky; top: 0; height: 100vh;
      backdrop-filter: var(--glass-blur);
    }

    .sidebar-logo {
      display: flex; align-items: center; gap: 16px;
      padding: 32px 24px;
      
      .logo-container {
        display: flex; align-items: center; justify-content: center;
        background: rgba(225, 202, 150, 0.1);
        border: 1px solid var(--glass-border);
        border-radius: 14px;
        width: 44px; height: 44px;
        box-shadow: var(--glow-gold);
      }
      
      .logo-bolt { color: var(--accent-gold); }
      
      .logo-text { 
        font-size: 22px; font-weight: 800; letter-spacing: -0.5px;
        color: var(--text-primary);
        span { color: var(--accent-gold); }
      }
    }

    .sidebar-nav {
      flex: 1; padding: 0 16px;
      display: flex; flex-direction: column; gap: 4px;
      overflow-y: auto;
    }

    .nav-section-title {
      font-size: 11px; font-weight: 700; color: var(--text-muted);
      text-transform: uppercase; letter-spacing: 1.5px;
      margin: 24px 0 8px 12px;
    }

    .nav-item {
      display: flex; align-items: center; gap: 12px;
      padding: 12px 16px; border-radius: 12px;
      font-size: 14px; font-weight: 600; color: var(--text-secondary);
      transition: all 0.3s; text-decoration: none;

      lucide-icon { opacity: 0.6; transition: all 0.3s; }

      &:hover { 
        background: rgba(225, 202, 150, 0.05); 
        color: var(--text-primary); 
        transform: translateX(5px);
        lucide-icon { opacity: 1; color: var(--accent-gold); }
      }

      &.active { 
        background: rgba(16, 185, 129, 0.1); 
        color: var(--accent-emerald);
        border: 1px solid rgba(16, 185, 129, 0.2);
        lucide-icon { opacity: 1; color: var(--accent-emerald); }
      }
    }

    .admin-link { 
      &:hover { color: var(--accent-gold); }
      &.active { 
        background: rgba(225, 202, 150, 0.1); 
        color: var(--accent-gold);
        border-color: rgba(225, 202, 150, 0.2);
        lucide-icon { color: var(--accent-gold); }
      }
    }

    .sidebar-footer {
      padding: 24px 16px;
    }

    .user-card {
      display: flex; align-items: center; gap: 12px;
      padding: 12px; border-radius: 16px;
      background: rgba(255, 255, 255, 0.03);
      border: 1px solid var(--glass-border);
    }

    .user-avatar {
      width: 40px; height: 40px; border-radius: 12px; flex-shrink: 0;
      background: var(--gradient-gold);
      display: flex; align-items: center; justify-content: center;
      font-size: 16px; font-weight: 800; color: #121416;
      box-shadow: var(--glow-gold);
    }

    .user-meta { flex: 1; min-width: 0; display: flex; flex-direction: column; }
    .u-name { font-size: 14px; font-weight: 700; color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .u-role { font-size: 11px; color: var(--text-muted); font-weight: 500; }

    .logout-btn {
      background: rgba(244, 63, 94, 0.1); border: none; color: var(--accent-red);
      cursor: pointer; padding: 8px; border-radius: 10px;
      display: flex; align-items: center; justify-content: center;
      transition: all 0.3s;
      &:hover { background: var(--accent-red); color: white; transform: scale(1.1); }
    }

    .main-content { 
      flex: 1; overflow-y: auto; min-height: 100vh; 
      padding: 32px;
      background: radial-gradient(circle at top right, rgba(225, 202, 150, 0.05), transparent 400px),
                  radial-gradient(circle at bottom left, rgba(16, 185, 129, 0.03), transparent 400px);
    }
  `]
})
export class LayoutComponent {
  auth    = inject(AuthService);
  themeService = inject(ThemeService);
  user    = this.auth.currentUser;
  isAdmin = () => this.auth.isAdmin();

  // Lucide icon declarations
  readonly icons = { LayoutDashboard, Zap, BookOpen, Clock, Bell, User, Shield, LogOut, ChevronRight, Moon, Sun };
}
