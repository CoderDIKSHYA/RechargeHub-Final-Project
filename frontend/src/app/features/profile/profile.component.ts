import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page">
      <div class="page-header">
        <h2>👤 My Profile</h2>
        <p>Your account information</p>
      </div>
      <div class="profile-layout">
        <div class="card avatar-card">
          <div class="avatar">
            <img *ngIf="user()?.profilePictureUrl" [src]="user()?.profilePictureUrl" alt="Profile"/>
            <div class="avatar-placeholder" *ngIf="!user()?.profilePictureUrl">
              {{ user()?.name?.charAt(0)?.toUpperCase() }}
            </div>
          </div>
          <h3>{{ user()?.name }}</h3>
          <p>{{ user()?.email }}</p>
          <span class="badge" [class]="user()?.role === 'ROLE_ADMIN' ? 'admin' : 'user'">
            {{ user()?.role === 'ROLE_ADMIN' ? '🛡️ Admin' : '👤 User' }}
          </span>
        </div>
        <div class="card details-card">
          <h3>Account Details</h3>
          <div class="detail-row"><span class="label">Full Name</span><span class="value">{{ user()?.name }}</span></div>
          <div class="detail-row"><span class="label">Email</span><span class="value">{{ user()?.email }}</span></div>
          <div class="detail-row"><span class="label">Phone</span><span class="value">{{ user()?.phoneNumber || 'Not set' }}</span></div>
          <div class="detail-row">
            <span class="label">Role</span>
            <span class="value">
              <span class="badge" [class]="user()?.role === 'ROLE_ADMIN' ? 'admin' : 'user'">{{ user()?.role }}</span>
            </span>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .page { padding: 24px; max-width: 900px; margin: 0 auto; }
    .page-header { margin-bottom: 24px; h2 { font-size: 22px; font-weight: 700; color: var(--text-gold); } p { color: var(--text-muted); font-size: 14px; margin-top: 4px; } }
    .profile-layout { display: grid; grid-template-columns: 260px 1fr; gap: 20px; }
    .card { background: var(--bg-card); border: 1px solid var(--glass-border); border-radius: 16px; padding: 24px; backdrop-filter: var(--glass-blur); }
    .avatar-card { text-align: center; h3 { font-size: 18px; font-weight: 700; margin: 16px 0 4px; color: var(--text-primary); } p { color: var(--text-muted); font-size: 14px; margin-bottom: 12px; } }
    .avatar { width: 80px; height: 80px; border-radius: 50%; margin: 0 auto; overflow: hidden; border: 3px solid var(--accent-gold);
      img { width: 100%; height: 100%; object-fit: cover; }
    }
    .avatar-placeholder { width: 100%; height: 100%; background: var(--gradient-gold); display: flex; align-items: center; justify-content: center; font-size: 32px; font-weight: 700; color: #121416; }
    .details-card h3 { font-size: 16px; font-weight: 600; margin-bottom: 20px; color: var(--text-gold); }
    .detail-row { display: flex; justify-content: space-between; align-items: center; padding: 14px 0; border-bottom: 1px solid var(--glass-border); &:last-child { border-bottom: none; } }
    .label { font-size: 13px; color: var(--text-muted); } .value { font-size: 14px; color: var(--text-primary); font-weight: 500; }
    .badge { display: inline-block; padding: 3px 10px; border-radius: 20px; font-size: 11px; font-weight: 700;
      &.admin { background: rgba(124,58,237,0.15); color: #c4b5fd; }
      &.user  { background: rgba(34,197,94,0.15);  color: #86efac; }
    }
  `]
})
export class ProfileComponent {
  private auth = inject(AuthService);
  user = this.auth.currentUser;
}
