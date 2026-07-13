import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/** Prevents non-admin users from accessing admin routes. */
export const adminGuard: CanActivateFn = () => {
  const auth   = inject(AuthService);
  const router = inject(Router);

  if (!auth.isLoggedIn()) {
    router.navigate(['/admin/login']);
    return false;
  }

  if (auth.isAdmin()) return true;

  router.navigate(['/dashboard']);
  return false;
};
