import { CommonModule } from '@angular/common';
import { Component} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EvaluacionService } from '../services/evaluacion.service';
import { EventosService } from '../services/eventos.service';
import { Notificaciones } from '../notificaciones/notificaciones';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-evaluar-eventos',
  imports: [CommonModule, FormsModule, RouterLink, Notificaciones],
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
  observaciones: string = '';
  actaComite: File | null = null;

  paginaActual = 1;
  elementosPorPagina = 5; 

  // Modal de mensajes
  showMessageModal = false;
  messageType: 'success' | 'error' = 'success';
  messageText = '';
  messageTitle = '';

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

  constructor(
    private evaluacionService: EvaluacionService, 
    public eventosService: EventosService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    const idSecretaria = this.authService.getUserId();
    if (!idSecretaria) {
      console.error("ID de secretaria no encontrado");
      this.eventos = [];
      return;
    }
    this.evaluacionService.listarPendientes(idSecretaria).subscribe({
      next: (data) => {      
        
        this.eventos = data.map((e: any) => ({
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

  // Métodos para el modal de mensajes
  showMessage(type: 'success' | 'error', title: string, message: string) {
    this.messageType = type;
    this.messageTitle = title;
    this.messageText = message;
    this.showMessageModal = true;
  }

  closeMessageModal() {
    this.showMessageModal = false;
    this.messageText = '';
    this.messageTitle = '';
  }

  eventosFiltrados() {
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
    this.decision = '';
    this.observaciones = '';
    this.actaComite = null;
    this.modalEvaluarVisible = true;
  }

  cerrarModalEvaluacion() {
    this.modalEvaluarVisible = false;
    this.decision = '';
    this.observaciones = '';
    this.actaComite = null;
  }

  cerrarMensaje() {
    this.mensajeVisible = false;
    this.mensajeTexto = '';
  }

  onActaChange(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0] || null;
    if (file && file.type !== 'application/pdf') {
      this.mensajeTexto = 'El acta debe ser un archivo PDF';
      this.mensajeVisible = true;
      (event.target as HTMLInputElement).value = '';
      return;
    }
    this.actaComite = file;
  }

  confirmarEvaluacion() {
    if (!this.eventoSeleccionado || !this.decision) {
      this.showMessage('error', 'Error en evaluación', 'Debe seleccionar una decisión antes de confirmar');
      return;
    }

    // Validar observaciones obligatorias para rechazo
    if (this.decision === 'rechazado' && (!this.observaciones || !this.observaciones.trim())) {
      this.showMessage('error', 'Error en evaluación', 'Las observaciones son obligatorias para rechazar un evento');     
      return;
    }

    if (this.decision === 'aprobado' && (!this.actaComite)){
      this.showMessage('error', 'Error en evaluación', 'El acta del cómite es obligatoria para aprobar un evento');     
      return;
    }

    const idSecretaria = this.authService.getUserId();
    if (!idSecretaria) {
      this.mensajeTexto = 'No se encontró sesión activa';
      this.mensajeVisible = true;
      return;
    }

    // Crear FormData
    const formData = new FormData();
    formData.append('decision', this.decision);
    formData.append('idSecretaria', idSecretaria.toString());
    if (this.observaciones){
      formData.append('observaciones', this.observaciones);
    }    
    
    if (this.actaComite) {
      formData.append('actaComite', this.actaComite, this.actaComite.name);
    }

    // Llamar al servicio apropiado
    const accion$ = this.decision === 'aprobado'
      ? this.evaluacionService.aprobar(this.eventoSeleccionado.codigo.toString(),formData)
      : this.evaluacionService.rechazar(this.eventoSeleccionado.codigo.toString(), formData);

    accion$.subscribe({
      next: (resp) => {
        // Eliminar de la lista local
        this.eventos = this.eventos.filter(e => e.codigo !== this.eventoSeleccionado.codigo);
        // Mensaje de éxito
        this.showMessage('success', '¡Evaluación Exitosa!', resp?.mensaje || 'El evento ha sido evaluado exitosamente');
        this.cerrarModalEvaluacion();
      },
      error: (err) => {
        this.mensajeTexto = err?.error?.mensaje || err?.error?.message || 'No se pudo completar la acción';
        this.mensajeVisible = true;
      }
    });
  }
}