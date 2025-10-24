import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink, Router } from '@angular/router';

@Component({
  selector: 'app-home',
  imports: [CommonModule, RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home {
  usuario: any = null;

  constructor(private router: Router) {}

  ngOnInit(): void {
    const userData = localStorage.getItem('auth_user');
    if (userData) {
      this.usuario = JSON.parse(userData);
    } else {
      this.router.navigateByUrl('/login');
    }
  }

}
