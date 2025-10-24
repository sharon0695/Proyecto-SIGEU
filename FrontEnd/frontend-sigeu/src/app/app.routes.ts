import { Routes } from '@angular/router';
import { CrearCuenta } from './crear-cuenta/crear-cuenta';
import { Login } from './login/login';
import { Organizaciones } from './organizaciones/organizaciones';
import { Perfil } from './perfil/perfil';
import { RecuperarContrasena } from './recuperar-contrasena/recuperar-contrasena';
import { Home } from './home/home';
import { Eventos } from './eventos/eventos';
import { AuthService } from './services/auth.service';
import { inject } from '@angular/core';
import { HomeS } from './home-s/home-s';
import { RoleGuard } from './guards/role-guard';
import { EvaluarEventos } from './evaluar-eventos/evaluar-eventos';

export const routes: Routes = [
  { path: '', component: Login},
  { path: 'crearCuenta', component: CrearCuenta },
  { path: 'login', component: Login},
  { path: 'homeO', component: Home, canActivate: [RoleGuard], data: { roles: ['docente', 'estudiante'] }},
  { path: 'homeS', component: HomeS, canActivate: [RoleGuard], data: { roles: ['secretaria_academica'] }},
  { path: 'eventos', component: Eventos, canActivate: [RoleGuard], data: { roles: ['docente', 'estudiante'] }},
  { path: 'organizacionExt', component: Organizaciones, canActivate: [RoleGuard], data: { roles: ['docente', 'estudiante'] }},
  { path: 'perfil', component: Perfil, canActivate: [() => !!inject(AuthService).isAuthenticated() || (location.href = '/login', false)]},
  { path: 'recuperar-contrasena', component: RecuperarContrasena},
  { path: 'evaluar-evento', component: EvaluarEventos, canActivate: [RoleGuard], data: { roles: ['secretaria_academica'] }},
  { path: '**', redirectTo: ''}
];