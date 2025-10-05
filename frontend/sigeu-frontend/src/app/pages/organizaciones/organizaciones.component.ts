import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrganizacionesService, Organizacion } from '../../organizaciones.service';

@Component({
  selector: 'app-organizaciones',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './organizaciones.component.html',
  styleUrls: ['./organizaciones.component.scss']
})
export class OrganizacionesComponent implements OnInit {
  organizaciones: Organizacion[] = [];
  loading = true;

  constructor(private organizacionesService: OrganizacionesService) {}

  ngOnInit(): void {
    this.organizacionesService.listar().subscribe({
      next: (data) => { this.organizaciones = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }
}
