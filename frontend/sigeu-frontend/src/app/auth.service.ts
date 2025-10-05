import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';

interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresInMillis: number;
  identificacion: number;
  nombre: string;
  apellido: string;
  correoInstitucional: string;
  rol: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private http: HttpClient) {}

  login(credentials: { correoInstitucional: string; contrasena: string }) {
    return this.http.post<LoginResponse>('/api/usuarios/login', credentials).pipe(
      tap(res => localStorage.setItem('access_token', res.accessToken))
    );
  }

  logout() {
    return this.http.post<void>('/api/usuarios/logout', {}).pipe(
      tap(() => localStorage.removeItem('access_token'))
    );
  }

  isAuthenticated(): boolean {
    return !!localStorage.getItem('access_token');
  }
}
