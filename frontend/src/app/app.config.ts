import { ApplicationConfig, provideZoneChangeDetection, importProvidersFrom } from '@angular/core';
import { provideRouter, withViewTransitions } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { LucideAngularModule, LayoutDashboard, Zap, BookOpen, Clock, Bell, User, Shield, LogOut, ChevronRight, ChevronLeft, CheckCircle, Search, CreditCard, Wallet, Landmark, Moon, Sun, Smartphone, Tv, Wifi, Activity, History, ShieldAlert, TrendingUp, TrendingDown, IndianRupee, Plus, Edit2, Trash2, Loader2, X, Download, XCircle, UserPlus, Radio, Users, ChevronDown, ArrowRight, Key, Mail, Phone, Lock, Eye, EyeOff, Sparkles, Send, Bot, ArrowLeft, MessageCircle, Globe, ZapOff, MousePointer2, Gift } from 'lucide-angular';
import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withViewTransitions()),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideAnimations(),
    importProvidersFrom(
      LucideAngularModule.pick({ 
        LayoutDashboard, Zap, BookOpen, Clock, Bell, User, Shield, LogOut, 
        ChevronRight, ChevronLeft, CheckCircle, Search, CreditCard, Wallet, Landmark,
        Moon, Sun, Smartphone, Tv, Wifi, Activity, History, ShieldAlert,
        TrendingUp, TrendingDown, IndianRupee, Plus, Edit2, Trash2, Loader2, X,
        Download, XCircle, UserPlus, Radio, Users, ChevronDown, ArrowRight, Key, Mail,
        Phone, Lock, Eye, EyeOff, Sparkles, Send, Bot, ArrowLeft, MessageCircle, Globe, ZapOff, MousePointer2, Gift
      })
    )
  ]
};
