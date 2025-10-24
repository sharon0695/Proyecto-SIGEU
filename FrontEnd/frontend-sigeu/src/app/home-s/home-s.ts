import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-home-s',
  imports: [RouterLink],
  templateUrl: './home-s.html',
  styleUrl: './home-s.css'
})
export class HomeS {
  usuario: any = null;

  constructor(private router: Router) {}

  ngOnInit(): void {
    const userData = localStorage.getItem('auth_user');
    if (userData) {
      this.usuario = JSON.parse(userData);
      if (this.usuario.rol !== 'secretaria_academica') {
        this.redirigirSegunRol(this.usuario.rol);
      }
    } else {
      this.router.navigateByUrl('/login');
    }
  }

  redirigirSegunRol(rol: string): void {
    if (rol === 'estudiante' || 'docente') {
      this.router.navigateByUrl('/homeO');
    } else {
      this.router.navigateByUrl('/login');
    }
  }
}
