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

  ngOnInit() {
    this.cargarEventos();
  }

  cargarEventos() {
    // Ejemplo: normalmente vendrán de un servicio
    const recibidos = [
      { codigo: 'E001', nombre: 'Congreso Innovación', descripcion: 'Evento académico sobre innovación.', fecha: '2025-11-12', organizador: 'Juan Pérez' },
      { codigo: 'E002', nombre: 'Feria Tecnológica', descripcion: 'Exposición de proyectos de ingeniería.', fecha: '2025-11-20', organizador: 'María López' },
      { codigo: 'E003', nombre: 'Semana Cultural', descripcion: 'Muestras artísticas estudiantiles.', fecha: '2025-12-01', organizador: 'Ana Gómez' },
      { codigo: 'E004', nombre: 'Charla IA', descripcion: 'Inteligencia Artificial en la educación.', fecha: '2025-12-05', organizador: 'Carlos Díaz' },
      { codigo: 'E005', nombre: 'Taller Ética Profesional', descripcion: 'Reflexión sobre ética en la ingeniería.', fecha: '2025-12-10', organizador: 'Laura Torres' },
      { codigo: 'E006', nombre: 'Foro Ambiental', descripcion: 'Debate sobre sostenibilidad.', fecha: '2025-12-15', organizador: 'Luis Castro' },
    ];

    // Convertir la fecha a Date para que el pipe date no falle
    this.eventos = recibidos.map(e => ({
      ...e,
      // Si la fecha ya es Date, se mantiene; si es string, convertir a Date
      fecha: e.fecha ? new Date(e.fecha) : null
    }));
  }

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
