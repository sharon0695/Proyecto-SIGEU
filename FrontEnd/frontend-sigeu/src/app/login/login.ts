import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../services/auth.service';
import { Router, RouterLink } from "@angular/router";

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './css/main.css'
})
export class Login {
  form = { correoInstitucional: '', contrasena: '' };
  mensaje = '';
  esError = false;

  constructor(private auth: AuthService, private router: Router) {}
 
  onSubmit(event: Event) {
    event.preventDefault();
    this.auth.login(this.form).subscribe({
      next: (response) => {
        const rol = (response?.rol || '').trim().toLowerCase();

        if (rol === 'secretaria_academica') {
          this.router.navigateByUrl('/homeS');
        } else if(rol === 'estudiante' || 'docente'){
          this.router.navigateByUrl('/homeO');
        }
      },
      error: () => {
        this.esError = true;
        this.mensaje = 'Credenciales inválidas';
      },
    });
  }
  mostrarContrasena = false;

  toggleContrasena(): void {
    this.mostrarContrasena = !this.mostrarContrasena;
  }

}