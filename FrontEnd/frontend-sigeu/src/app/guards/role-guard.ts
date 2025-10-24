import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    const user = this.authService.getUser(); 
    const expectedRoles = route.data['roles'] as Array<string>;

    if (!user || !expectedRoles.includes(user.rol)) {
      alert('No tienes permisos para acceder a esta página.');
      this.router.navigate(['/login']);
      return false;
    }

    return true;
  }
}
