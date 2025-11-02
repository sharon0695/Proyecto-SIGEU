import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EvaluacionService } from '../services/evaluacion.service';
import { EventosService } from '../services/eventos.service';

@Component({
  selector: 'app-evaluar-eventos',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './evaluar-eventos.html',
  styleUrls: ['./evaluar-eventos.css']
})
export class EvaluarEventos {
  // 🔍 Filtros y búsqueda
  busqueda: string = '';
  filtroEstado: string = '';

  // 📋 Datos
  eventos: any[] = [];
  eventosFiltradosList: any[] = [];

  // ⚙️ Modales y mensajes
  modalEvaluarVisible = false;
  modalVerMasVisible = false;
  mensajeVisible = false;
  mensajeTexto: string = '';
  mostrarNotificaciones: boolean = false;
  notificaciones: string[] = [];

  // 🔘 Control de evaluación
  eventoSeleccionado: any = null;
  detallesEvento: any = null;
  decision: string = '';

  // 📄 Paginación
  paginaActual = 1;
  elementosPorPagina = 3;

  constructor(
    private evaluacionService: EvaluacionService,
    public eventosService: EventosService
  ) {}

  get totalPaginas(): number {
    return Math.ceil(this.eventosFiltradosList.length / this.elementosPorPagina);
  }

  ngOnInit() {
    this.evaluacionService.listarPendientes().subscribe({
      next: (data) => {
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
        this.filtrarEventos();
      },
      error: () => {
        this.eventos = [];
      }
    });
  }

  // 🔍 Filtrado de eventos
  filtrarEventos() {
    this.eventosFiltradosList = this.eventos.filter(e => {
      const coincideBusqueda = e.nombre.toLowerCase().includes(this.busqueda.toLowerCase());
      const coincideEstado = this.filtroEstado ? e.estado === this.filtroEstado : true;
      return coincideBusqueda && coincideEstado;
    });
  }

  paginaAnterior() {
    if (this.paginaActual > 1) this.paginaActual--;
  }

  paginaSiguiente() {
    if (this.paginaActual < this.totalPaginas) this.paginaActual++;
  }

  // 🔎 Ver detalles de evento
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

  mostrarMensajeNotificacion(mensaje: string) {
    this.notificaciones.push(mensaje);
    this.mostrarNotificaciones = true;
    setTimeout(() => {
      this.mostrarNotificaciones = false;
      this.notificaciones = [];
    }, 3000);
  }

  confirmarEvaluacion() {
    if (!this.eventoSeleccionado || !this.decision) return;

    const codigo = this.eventoSeleccionado.codigo;
    const accion$ = this.decision === 'aprobado'
      ? this.evaluacionService.aprobar(codigo)
      : this.evaluacionService.rechazar(codigo);

    accion$.subscribe({
      next: (resp) => {
        this.eventos = this.eventos.filter(e => e.codigo !== codigo);
        this.filtrarEventos();
        this.mostrarMensajeNotificacion(resp?.mensaje || `Evento ${this.decision}`);
        this.cerrarModalEvaluacion();
      },
      error: (err) => {
        this.mostrarMensajeNotificacion(err?.error?.message || 'No se pudo completar la acción');
      }
    });
  }
}
