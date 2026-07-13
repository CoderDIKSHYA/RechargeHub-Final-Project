import { Injectable, signal, effect } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  isDarkMode = signal<boolean>(true);

  constructor() {
    const stored = localStorage.getItem('rechargehub-theme');
    if (stored) {
      this.isDarkMode.set(stored === 'dark');
    } else {
      // Default to dark mode or system preference
      const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
      this.isDarkMode.set(prefersDark);
    }

    effect(() => {
      const isDark = this.isDarkMode();
      localStorage.setItem('rechargehub-theme', isDark ? 'dark' : 'light');
      if (isDark) {
        document.body.classList.add('dark-theme');
        document.body.classList.remove('light-theme');
      } else {
        document.body.classList.add('light-theme');
        document.body.classList.remove('dark-theme');
      }
    });
  }

  toggleTheme() {
    this.isDarkMode.set(!this.isDarkMode());
  }
}
