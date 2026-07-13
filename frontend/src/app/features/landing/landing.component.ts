import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { LucideAngularModule, Zap, Shield, Smartphone, CreditCard, ChevronRight, Activity, Users, Globe, Gift, CheckCircle, ArrowRight, MousePointer2 } from 'lucide-angular';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, RouterLink, LucideAngularModule],
  template: `
    <div class="landing-page">
      <!-- Background Shapes -->
      <div class="floating-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
      </div>

      <!-- Navbar -->
      <nav class="landing-nav" [class.scrolled]="isScrolled">
        <div class="logo">
          <div class="logo-container">
            <lucide-icon name="zap" [size]="24" class="logo-bolt"></lucide-icon>
          </div>
          <span class="logo-text">RechargeHub</span>
        </div>
        <div class="nav-links">
          <a routerLink="/auth/login" class="nav-link">Sign In</a>
          <a routerLink="/auth/register" class="btn btn-primary nav-btn shadow-gold">Get Started</a>
        </div>
      </nav>

      <!-- Hero Section -->
      <header class="hero">
        <div class="hero-content" [class.reveal]="true">
          <div class="badge-premium">
            <lucide-icon name="check-circle" [size]="14"></lucide-icon>
            <span>Trusted by {{ totalUsers() }}+ Users</span>
          </div>
          <h1 class="hero-title">
            Redefining the <br/>
            <span class="text-gradient">Financial Pulse</span>
          </h1>
          <p class="hero-subtitle">
            Experience the next generation of mobile recharges and bill payments with our high-fidelity secure ecosystem.
          </p>
          <div class="hero-actions">
            <a routerLink="/auth/register" class="btn btn-primary btn-large glow-btn">
              Get Started Now
              <lucide-icon name="chevron-right" [size]="20"></lucide-icon>
            </a>
            <div class="scroll-prompt">
              <lucide-icon name="mouse-pointer2" [size]="18" class="mouse-icon"></lucide-icon>
              <span>Scroll to Explore</span>
            </div>
          </div>
        </div>
        
        <div class="hero-visual">
          <div class="glass-card hero-mockup parallax" [style.transform]="parallaxTransform">
            <div class="mockup-header">
              <div class="mockup-dot red"></div>
              <div class="mockup-dot yellow"></div>
              <div class="mockup-dot green"></div>
            </div>
            <div class="mockup-body">
              <div class="mockup-balance">
                <span>Total Platform Savings</span>
                <h2>₹ {{ totalSavings() | number }}</h2>
              </div>
              <div class="mockup-list">
                <div class="m-list-item">
                  <div class="m-icon gold"><lucide-icon name="zap" [size]="18"></lucide-icon></div>
                  <div class="m-text">
                    <span class="m-title">Jio Recharge</span>
                    <span class="m-sub">Success • Just now</span>
                  </div>
                  <span class="m-amt">+₹499</span>
                </div>
                <div class="m-list-item">
                  <div class="m-icon emerald"><lucide-icon name="shield" [size]="18"></lucide-icon></div>
                  <div class="m-text">
                    <span class="m-title">Safety Shield</span>
                    <span class="m-sub">Active Protection</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="glow-bg"></div>
        </div>
      </header>

      <!-- Quick Actions / Features Grid -->
      <section class="quick-hub reveal-on-scroll">
        <div class="hub-header">
          <span class="section-tag">Utility Hub</span>
          <h2>One App. <span>Infinite Possibilities.</span></h2>
        </div>
        <div class="hub-grid">
          <div class="hub-card" *ngFor="let act of quickActions">
            <div class="hub-icon" [style.background]="act.bg">
              <lucide-icon [name]="act.icon" [size]="28"></lucide-icon>
            </div>
            <h3>{{ act.title }}</h3>
            <p>{{ act.desc }}</p>
            <lucide-icon name="arrow-right" [size]="18" class="hub-arrow"></lucide-icon>
          </div>
        </div>
      </section>

      <!-- Stats Section -->
      <section class="stats-section reveal-on-scroll">
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-num">{{ totalUsers() }}+</div>
            <div class="stat-label">Happy Users</div>
          </div>
          <div class="stat-item">
            <div class="stat-num">{{ successRate() }}</div>
            <div class="stat-label">Success Rate</div>
          </div>
          <div class="stat-item">
            <div class="stat-num">24/7</div>
            <div class="stat-label">Human Support</div>
          </div>
        </div>
      </section>

      <!-- Promotions / Offers -->
      <section class="offers-section reveal-on-scroll">
        <div class="offers-content">
          <div class="offer-card premium-glass">
            <div class="offer-badge">Limited Offer</div>
            <h2>Get <span>20% Cashback</span> on your first Airtel recharge.</h2>
            <p>Use code <strong>RECHARGE20</strong> at checkout. T&C Apply.</p>
            <button class="btn btn-secondary">Claim Now</button>
          </div>
          <div class="offers-visual">
            <div class="floating-card c1"><lucide-icon name="gift" [size]="32"></lucide-icon></div>
            <div class="floating-card c2"><lucide-icon name="credit-card" [size]="32"></lucide-icon></div>
          </div>
        </div>
      </section>
      
      <!-- Footer -->
      <footer class="landing-footer">
        <div class="footer-content">
          <div class="f-brand">
            <span class="logo-text">RechargeHub</span>
            <p>The premium standard for digital mobility.</p>
          </div>
          <div class="f-links">
            <div class="f-col">
              <h4>Product</h4>
              <a href="#">Features</a>
              <a href="#">Security</a>
              <a href="#">API Docs</a>
            </div>
            <div class="f-col">
              <h4>Company</h4>
              <a href="#">About</a>
              <a href="#">Careers</a>
              <a href="#">Blog</a>
            </div>
          </div>
        </div>
        <div class="footer-bottom">
          <p>&copy; 2026 RechargeHub. All rights reserved.</p>
        </div>
      </footer>
    </div>
  `,
  styles: [`
    .landing-page {
      min-height: 100vh;
      background: #03010A;
      overflow-x: hidden;
      font-family: 'Plus Jakarta Sans', system-ui, sans-serif;
      position: relative;
    }

    .floating-shapes {
      position: absolute; width: 100%; height: 100%; overflow: hidden; pointer-events: none;
      .shape { position: absolute; border-radius: 50%; filter: blur(100px); opacity: 0.15; }
      .shape-1 { width: 500px; height: 500px; background: var(--accent-gold); top: -200px; right: -100px; animation: float 10s infinite alternate; }
      .shape-2 { width: 400px; height: 400px; background: var(--accent-emerald); bottom: 100px; left: -100px; animation: float 12s infinite alternate-reverse; }
      .shape-3 { width: 300px; height: 300px; background: #6366f1; top: 30%; left: 50%; animation: float 15s infinite; }
    }

    @keyframes float { 
      0% { transform: translate(0, 0); }
      100% { transform: translate(40px, 40px); }
    }

    /* Navbar */
    .landing-nav {
      display: flex; justify-content: space-between; align-items: center;
      padding: 24px 8%; position: fixed; top: 0; left: 0; right: 0; z-index: 1000;
      transition: all 0.4s;
      &.scrolled { background: rgba(3, 1, 10, 0.8); backdrop-filter: blur(20px); border-bottom: 1px solid rgba(255,255,255,0.05); padding: 16px 8%; }
    }

    .nav-links {
      display: flex; align-items: center; gap: 32px;
      .nav-link { color: #94A3B8; font-weight: 700; text-decoration: none; font-size: 14px; transition: color 0.3s; &:hover { color: white; } }
      .nav-btn { padding: 12px 28px; font-weight: 800; font-size: 14px; border-radius: 50px; }
    }

    .logo {
      display: flex; align-items: center; gap: 14px;
      .logo-container {
        width: 44px; height: 44px; background: rgba(225, 202, 150, 0.1);
        border-radius: 14px; display: flex; align-items: center; justify-content: center;
        border: 1px solid rgba(225, 202, 150, 0.2);
      }
      .logo-text { font-size: 24px; font-weight: 900; background: var(--gradient-gold); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
    }

    /* Hero */
    .hero {
      min-height: 100vh; display: flex; align-items: center; padding: 0 8%;
      padding-top: 100px; gap: 40px;
    }
    .hero-content { flex: 1.2; opacity: 0; transform: translateY(30px); transition: all 1s cubic-bezier(0.16, 1, 0.3, 1); }
    .hero-content.reveal { opacity: 1; transform: translateY(0); }

    .badge-premium {
      display: inline-flex; align-items: center; gap: 8px; padding: 8px 20px;
      background: rgba(16, 185, 129, 0.05); border: 1px solid rgba(16, 185, 129, 0.2);
      border-radius: 50px; color: var(--accent-emerald); font-size: 13px; font-weight: 700; margin-bottom: 32px;
    }

    .hero-title {
      font-size: 80px; font-weight: 900; line-height: 1; letter-spacing: -4px; color: white; margin-bottom: 32px;
      .text-gradient { background: var(--gradient-gold); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
    }
    .hero-subtitle { font-size: 20px; color: #94A3B8; line-height: 1.6; max-width: 520px; margin-bottom: 48px; }
    
    .hero-actions { display: flex; align-items: center; gap: 32px; }
    .glow-btn { border-radius: 50px; padding: 20px 40px; font-size: 18px; font-weight: 800; box-shadow: 0 10px 30px rgba(225, 202, 150, 0.2); }
    
    .scroll-prompt {
      display: flex; align-items: center; gap: 12px; color: #475569; font-size: 14px; font-weight: 600;
      .mouse-icon { animation: bounce 2s infinite; }
    }
    @keyframes bounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-5px); } }

    /* Hero Visual */
    .hero-visual { flex: 1; position: relative; }
    .hero-mockup {
      width: 420px; padding: 24px; border-radius: 32px;
      background: rgba(255, 255, 255, 0.03); border: 1px solid rgba(255,255,255,0.1);
      box-shadow: 0 40px 100px -20px rgba(0,0,0,0.5);
    }
    .mockup-balance {
      background: var(--gradient-gold); padding: 32px; border-radius: 20px; color: #121416; margin-bottom: 24px;
      span { font-size: 14px; font-weight: 600; opacity: 0.7; }
      h2 { font-size: 36px; font-weight: 800; margin: 4px 0 0; }
    }
    .m-list-item {
      display: flex; align-items: center; gap: 16px; padding: 16px;
      background: rgba(255,255,255,0.02); border-radius: 16px; margin-bottom: 12px;
      .m-icon { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
      .gold { background: rgba(225, 202, 150, 0.1); color: var(--accent-gold); }
      .emerald { background: rgba(16, 185, 129, 0.1); color: var(--accent-emerald); }
      .m-text { flex: 1; .m-title { display: block; font-weight: 700; color: white; } .m-sub { font-size: 12px; color: #475569; } }
      .m-amt { font-weight: 800; color: var(--accent-gold); }
    }

    /* Hub Grid */
    .quick-hub { padding: 120px 8%; text-align: center; }
    .hub-header { margin-bottom: 64px;
      .section-tag { color: var(--accent-gold); text-transform: uppercase; font-size: 12px; font-weight: 900; letter-spacing: 3px; }
      h2 { font-size: 48px; font-weight: 900; color: white; margin-top: 12px; span { color: #94A3B8; } }
    }
    .hub-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 32px; }
    .hub-card {
      background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.05);
      padding: 48px 32px; border-radius: 32px; transition: all 0.4s; cursor: pointer; text-align: left;
      &:hover { transform: translateY(-12px); background: rgba(255,255,255,0.04); border-color: rgba(225, 202, 150, 0.2); 
        .hub-arrow { opacity: 1; transform: translateX(5px); }
      }
      .hub-icon { width: 64px; height: 64px; border-radius: 20px; display: flex; align-items: center; justify-content: center; color: white; margin-bottom: 24px; }
      h3 { font-size: 22px; font-weight: 800; color: white; margin-bottom: 12px; }
      p { color: #64748B; font-size: 15px; line-height: 1.6; }
      .hub-arrow { color: var(--accent-gold); opacity: 0; transition: all 0.3s; margin-top: 24px; }
    }

    /* Stats */
    .stats-section { padding: 100px 8%; background: rgba(255,255,255,0.01); border-top: 1px solid rgba(255,255,255,0.05); }
    .stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); text-align: center; }
    .stat-num { font-size: 64px; font-weight: 900; background: var(--gradient-gold); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
    .stat-label { font-size: 16px; color: #475569; font-weight: 700; margin-top: 8px; }

    /* Offers */
    .offers-section { padding: 120px 8%; }
    .offers-content { 
      background: linear-gradient(135deg, rgba(225, 202, 150, 0.1), rgba(16, 185, 129, 0.1));
      border-radius: 48px; padding: 80px; display: flex; align-items: center; gap: 64px; border: 1px solid rgba(255,255,255,0.05);
    }
    .offer-card { flex: 1; 
      .offer-badge { display: inline-block; padding: 6px 14px; background: var(--accent-gold); color: #121416; font-size: 12px; font-weight: 800; border-radius: 6px; margin-bottom: 24px; }
      h2 { font-size: 40px; font-weight: 900; color: white; margin-bottom: 20px; span { color: var(--accent-gold); } }
      p { color: #94A3B8; font-size: 18px; margin-bottom: 32px; }
    }
    .offers-visual { flex: 0.8; position: relative; height: 300px;
      .floating-card { position: absolute; width: 100px; height: 100px; background: rgba(255,255,255,0.05); backdrop-filter: blur(10px); border-radius: 24px; display: flex; align-items: center; justify-content: center; border: 1px solid rgba(255,255,255,0.1); color: var(--accent-gold); }
      .c1 { top: 0; left: 20%; animation: float 6s infinite alternate; }
      .c2 { bottom: 0; right: 20%; animation: float 8s infinite alternate-reverse; }
    }

    /* Footer */
    .landing-footer { padding: 100px 8% 40px; border-top: 1px solid rgba(255,255,255,0.05); }
    .footer-content { display: flex; justify-content: space-between; margin-bottom: 80px; }
    .f-brand { max-width: 300px; p { color: #475569; margin-top: 16px; font-weight: 600; } }
    .f-links { display: flex; gap: 100px; }
    .f-col h4 { color: white; margin-bottom: 24px; font-weight: 800; }
    .f-col a { display: block; color: #475569; margin-bottom: 12px; text-decoration: none; font-weight: 600; transition: color 0.3s; &:hover { color: var(--accent-gold); } }
    .footer-bottom { text-align: center; border-top: 1px solid rgba(255,255,255,0.03); padding-top: 40px; color: #1E293B; font-size: 13px; font-weight: 700; }

    /* Reveal animations */
    .reveal-on-scroll { opacity: 0; transform: translateY(40px); transition: all 0.8s cubic-bezier(0.16, 1, 0.3, 1); }
    .reveal-on-scroll.active { opacity: 1; transform: translateY(0); }
  `]
})
export class LandingComponent implements OnInit {
  private http = inject(HttpClient);
  
  isScrolled = false;
  parallaxTransform = '';
  
  // Real-time signals
  totalUsers = signal(500);
  totalSavings = signal(0);
  successRate = signal('99.9%');

  quickActions = [
    { title: 'Mobile Recharge', desc: 'Instant activation for all major operators nationwide.', icon: 'smartphone', bg: 'rgba(225, 202, 150, 0.2)' },
    { title: 'Utility Bills', desc: 'Pay Electricity, Water, and Gas bills with one tap.', icon: 'zap', bg: 'rgba(16, 185, 129, 0.2)' },
    { title: 'Money Transfer', desc: 'Secure bank-grade transfers with zero hidden fees.', icon: 'globe', bg: 'rgba(99, 102, 241, 0.2)' },
    { title: 'Safety Shield', desc: 'Advanced 2FA and encryption for every transaction.', icon: 'shield', bg: 'rgba(244, 63, 94, 0.2)' }
  ];

  ngOnInit() {
    this.fetchPublicStats();
    
    // Initial reveal for hero
    setTimeout(() => {
       const hero = document.querySelector('.hero-content');
       if (hero) hero.classList.add('reveal');
    }, 100);

    window.addEventListener('scroll', () => {
      this.isScrolled = window.scrollY > 50;
      this.parallaxTransform = `perspective(1000px) rotateY(-15deg) translateY(${window.scrollY * -0.1}px)`;
      
      const reveals = document.querySelectorAll('.reveal-on-scroll');
      reveals.forEach(el => {
        const top = el.getBoundingClientRect().top;
        if (top < window.innerHeight - 100) el.classList.add('active');
      });
    });
  }

  fetchPublicStats() {
    this.http.get<any>(`${environment.apiUrl}/users/public/stats`).subscribe({
      next: (res) => {
        this.totalUsers.set(res.totalUsers);
        this.totalSavings.set(res.totalSavings);
        this.successRate.set(res.successRate);
      },
      error: (err) => {
        console.warn('Could not fetch real-time stats, using defaults.', err);
      }
    });
  }
}
