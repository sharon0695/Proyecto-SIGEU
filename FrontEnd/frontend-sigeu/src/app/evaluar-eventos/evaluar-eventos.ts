import { CommonModule } from '@angular/common';
import { Component} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-evaluar-eventos',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './evaluar-eventos.html',
  styleUrl: './evaluar-eventos.css'
})
export class EvaluarEventos {
  busqueda: string = '';
  filtroEstado: string = '';

  eventos: any[] = [];
  eventosPaginados: any[] = [];
  modalEvaluarVisible = false;
  modalVerMasVisible = false;
  eventoSeleccionado: any = null;
  decision: string = '';

  paginaActual = 1;
  elementosPorPagina = 3; 

  get totalPaginas(): number {
    return Math.ceil(this.eventos.length / this.elementosPorPagina);
  }

  paginaAnterior() {
    if (this.paginaActual > 1) {
      this.paginaActual--;
    }
  }

  paginaSiguiente() {
    if (this.paginaActual < this.totalPaginas) {
      this.paginaActual++;
    }
  }

  ngOnInit() {}

  eventosFiltrados() {
    // temporalmente se simula una lista
    return this.eventos || [];
  }

  verDetalles(evento: any) {
    this.eventoSeleccionado = evento;
    this.modalVerMasVisible = true;
  }

  cerrarModalVerMas() {
    this.modalVerMasVisible = false;
  }

  abrirModalEvaluacion(evento: any) {
    this.eventoSeleccionado = evento;
    this.modalEvaluarVisible = true;
  }

  cerrarModalEvaluacion() {
    this.modalEvaluarVisible = false;
    this.decision = '';
  }

  confirmarEvaluacion() {
    console.log(`Evento ${this.eventoSeleccionado.nombre} -> ${this.decision}`);
    this.cerrarModalEvaluacion();
  }

}
