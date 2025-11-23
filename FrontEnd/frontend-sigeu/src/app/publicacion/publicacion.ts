import { Component} from '@angular/core';
import { EventosService } from '../services/eventos.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Notificaciones } from '../notificaciones/notificaciones';

interface EventoPublicado {
  codigo: number;
  nombre: string;
  descripcion: string;
  tipo: string;
  fecha: string;
  hora_inicio: string;
  hora_fin: string;
  espacios: {
    nombreEspacio: string;
    horaInicio: string;
    horaFin: string;
  }[];
}

@Component({
  selector: 'app-publicacion-eventos',
  imports: [CommonModule, FormsModule, Notificaciones, RouterLink],
  templateUrl: './publicacion.html',
  styleUrls: ['./publicacion.css']
})
export class Publicacion {

  eventos: EventoPublicado[] = [];
  loading = false;
  error = '';
  eventosPaginados: any[] = [];

  paginaActual: number = 1;
  elementosPorPagina: number = 9;

  constructor(private eventosService: EventosService) {}

  ngOnInit() {
    this.cargarEventos();
  }

  cargarEventos() {
    this.loading = true;
    this.eventosService.listarEventosPublicados().subscribe({
      next: (data: any) => {
        this.eventos = data;
        this.loading = false;
        this.eventos.sort((a, b) =>
          new Date(b.fecha).getTime() - new Date(a.fecha).getTime()
        );

        this.actualizarPaginado();
      },
      error: () => {
        this.error = 'No se pudieron cargar los eventos publicados.';
        this.loading = false;
      }
    });
  }
  get totalPaginas(): number {
    return Math.ceil(this.eventos.length / this.elementosPorPagina);
  }

  actualizarPaginado() {
    const inicio = (this.paginaActual - 1) * this.elementosPorPagina;
    const fin = inicio + this.elementosPorPagina;
    this.eventosPaginados = this.eventos.slice(inicio, fin);
  }

  paginaAnterior() {
    if (this.paginaActual > 1) {
      this.paginaActual--;
      this.actualizarPaginado();
    }
  }

  paginaSiguiente() {
    if (this.paginaActual < this.totalPaginas) {
      this.paginaActual++;
      this.actualizarPaginado();
    }
  }
}
