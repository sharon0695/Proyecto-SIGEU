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

export const routes: Routes = [
  { path: '', component: Login},
  { path: 'crearCuenta', component: CrearCuenta },
  { path: 'login', component: Login},
  { path: 'homeO', component: Home, canActivate: [() => !!inject(AuthService).isAuthenticated() ]},
  { path: 'homeS', component: HomeS, canActivate: [() => !!inject(AuthService).isAuthenticated()]},
  { path: 'eventos', component: Eventos, canActivate: [() => !!inject(AuthService).isAuthenticated() || (location.href = '/login', false)]},
  { path: 'organizacionExt', component: Organizaciones, canActivate: [() => !!inject(AuthService).isAuthenticated() || (location.href = '/login', false)]},
  { path: 'perfil', component: Perfil, canActivate: [() => !!inject(AuthService).isAuthenticated() || (location.href = '/login', false)]},
  { path: 'recuperar-contrasena', component: RecuperarContrasena},
  { path: '**', redirectTo: ''}
];