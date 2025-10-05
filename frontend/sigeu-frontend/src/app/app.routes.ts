import { Routes } from '@angular/router';
import { authGuard } from './auth.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent) },
  { path: 'organizaciones', canActivate: [authGuard], loadComponent: () => import('./pages/organizaciones/organizaciones.component').then(m => m.OrganizacionesComponent) },
  { path: '', pathMatch: 'full', redirectTo: 'organizaciones' },
  { path: '**', redirectTo: '' }
];
