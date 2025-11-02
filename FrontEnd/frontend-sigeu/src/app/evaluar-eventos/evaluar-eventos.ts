import { CommonModule } from '@angular/common';
import { Component} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EvaluacionService } from '../services/evaluacion.service';
import { EventosService } from '../services/eventos.service';

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
  mensajeVisible = false;
  mensajeTexto: string = '';
  eventoSeleccionado: any = null;
  detallesEvento: any = null;
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

  constructor(private evaluacionService: EvaluacionService, public eventosService: EventosService) {}

  ngOnInit() {
    this.evaluacionService.listarPendientes().subscribe({
      next: (data) => {
        // Normalizar datos mínimos esperados por la vista
        this.eventos = (data || []).map((e: any) => ({
          codigo: e.codigo,
          nombre: e.nombre,
          descripcion: e.descripcion,
          tipo: e.tipo,
          fecha: e.fecha,
          hora_inicio: e.hora_inicio,
          hora_fin: e.hora_fin,
          estado: e.estado,
          organizador: e.organizadorNombre || '-',
        }));
      },
      error: () => {
        this.eventos = [];
      }
    });
  }

  eventosFiltrados() {
    // temporalmente se simula una lista
    return this.eventos || [];
  }

  verDetalles(evento: any) {
    this.eventoSeleccionado = evento;
    this.evaluacionService.obtenerDetalle(evento.codigo).subscribe({
      next: (det) => {
        this.detallesEvento = det;
        this.modalVerMasVisible = true;
      },
      error: () => {
        this.detallesEvento = null;
        this.modalVerMasVisible = true;
      }
    });
  }

  cerrarModalVerMas() {
    this.modalVerMasVisible = false;
    this.detallesEvento = null;
  }

  abrirModalEvaluacion(evento: any) {
    this.eventoSeleccionado = evento;
    this.modalEvaluarVisible = true;
  }

  cerrarModalEvaluacion() {
    this.modalEvaluarVisible = false;
    this.decision = '';
  }

  cerrarMensaje() {
    this.mensajeVisible = false;
    this.mensajeTexto = '';
  }

  confirmarEvaluacion() {
    if (!this.eventoSeleccionado || !this.decision) return;

    const codigo = this.eventoSeleccionado.codigo;
    const accion$ = this.decision === 'aprobado'
      ? this.evaluacionService.aprobar(codigo)
      : this.evaluacionService.rechazar(codigo);

    accion$.subscribe({
      next: (resp) => {
        // Eliminar de la lista local
        this.eventos = this.eventos.filter(e => e.codigo !== codigo);
        // Mensaje de éxito en ventana
        this.mensajeTexto = resp?.mensaje || `Evento ${this.decision}`;
        this.mensajeVisible = true;
        this.cerrarModalEvaluacion();
      },
      error: (err) => {
        this.mensajeTexto = err?.error?.message || 'No se pudo completar la acción';
        this.mensajeVisible = true;
      }
    });
  }

}
