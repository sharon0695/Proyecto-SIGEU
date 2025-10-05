import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  correoInstitucional = '';
  contrasena = '';
  loading = false;
  error = '';

  constructor(private auth: AuthService, private router: Router) {}

  submit() {
    this.loading = true;
    this.error = '';
    this.auth.login({ correoInstitucional: this.correoInstitucional, contrasena: this.contrasena })
      .subscribe({
        next: () => this.router.navigateByUrl('/organizaciones'),
        error: (e) => { this.error = 'Credenciales inválidas'; this.loading = false; }
      });
  }
}
