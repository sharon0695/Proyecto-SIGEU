import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
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
export class EvaluarEventos implements OnInit {
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
    this.cargarEventosPendientes();
  }

  private cargarEventosPendientes() {
    const idSecretaria = this.authService.getUserId();
    
    if (!idSecretaria) {
      console.error("❌ ID de secretaria no encontrado en el almacenamiento local");
      this.showMessage('error', 'Error de sesión', 'No se pudo obtener el ID de usuario. Por favor, cierre sesión y vuelva a iniciar.');
      this.eventos = [];
      return;
    }

    console.log("✅ ID de secretaria obtenido:", idSecretaria);

    this.evaluacionService.listarPendientes(idSecretaria).subscribe({
      next: (data) => {
        console.log("📦 Datos recibidos del backend:", data);
        
        if (!Array.isArray(data)) {
          console.error("❌ La respuesta del backend no es un array:", data);
          this.showMessage('error', 'Error de datos', 'La respuesta del servidor tiene un formato inesperado');
          this.eventos = [];
          return;
        }

        this.eventos = data.map((e: any) => {
          console.log("🔍 Evento individual:", e);
          
          return {
            codigo: e.codigo,
            nombre: e.nombre,
            descripcion: e.descripcion,
            tipo: e.tipo,
            fecha: e.fecha,
            hora_inicio: e.hora_inicio,
            hora_fin: e.hora_fin,
            estado: e.estado,
            organizador: e.organizadorNombre || 'No asignado'
          };
        });

        console.log("✅ Eventos procesados:", this.eventos);

        if (this.eventos.length === 0) {
          console.warn("⚠️ No hay eventos pendientes para esta secretaría");
        }
      },
      error: (err) => {
        console.error("❌ Error al cargar eventos:", err);
        
        let mensajeError = 'No se pudieron cargar los eventos pendientes';
        
        if (err.status === 404) {
          mensajeError = 'No se encontró el endpoint de evaluación';
        } else if (err.status === 403) {
          mensajeError = 'No tiene permisos para acceder a esta información';
        } else if (err.status === 0) {
          mensajeError = 'No se pudo conectar con el servidor. Verifique su conexión';
        } else if (err.error?.mensaje) {
          mensajeError = err.error.mensaje;
        }

        this.showMessage('error', 'Error al cargar eventos', mensajeError);
        this.eventos = [];
      }
    });
  }

  private contieneInyeccion(valor: string): boolean {
    if (!valor) return false;

    // Palabras o patrones comunes de inyección SQL o HTML
    const patronesPeligrosos = [
      /<script.*?>.*?<\/script>/i,  // scripts HTML
      /<[^>]+>/,                    // etiquetas HTML
      /['"`;]/,                     // comillas o punto y coma
      /\b(SELECT|INSERT|UPDATE|DELETE|DROP|UNION|ALTER|CREATE|EXEC|--|#)\b/i // SQL
    ];

    return patronesPeligrosos.some((patron) => patron.test(valor));
  }

  showMessage(type: 'success' | 'error', title: string, message: string) {
    this.messageType = type;
    this.messageTitle = title;
    this.messageText = message;
    this.showMessageModal = true;

    console.log(`📢 Mensaje mostrado: [${type.toUpperCase()}] ${title}: ${message}`);
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
    console.log("👁️ Viendo detalles del evento:", evento.codigo);
    this.eventoSeleccionado = evento;
    
    this.evaluacionService.obtenerDetalle(evento.codigo).subscribe({
      next: (det) => {
        console.log("📋 Detalles cargados:", det);
        this.detallesEvento = det;
        this.modalVerMasVisible = true;
      },
      error: (err) => {
        console.error("❌ Error al cargar detalles:", err);
        this.showMessage('error', 'Error', 'No se pudieron cargar los detalles del evento');
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
    console.log("📝 Abriendo modal de evaluación para:", evento.nombre);
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
      this.showMessage('error', 'Archivo inválido', 'El acta debe ser un archivo PDF');
      (event.target as HTMLInputElement).value = '';
      return;
    }
    this.actaComite = file;
    console.log("📎 Acta seleccionada:", file?.name);
  }

  confirmarEvaluacion() {
    if (!this.eventoSeleccionado || !this.decision) {
      this.showMessage('error', 'Error en evaluación', 'Debe seleccionar una decisión antes de confirmar');
      return;
    }

    if (this.decision === 'rechazado' && (!this.observaciones || !this.observaciones.trim())) {
      this.showMessage('error', 'Error en evaluación', 'Las observaciones son obligatorias para rechazar un evento');     
      return;
    }

    if (this.decision === 'rechazado' && (this.observaciones.trim().length < 10)) {
      this.showMessage('error', 'Error en evaluación', 'Las observaciones deben tener una justificación mínima de 10 caracteres');     
      return;
    }

    if (this.decision === 'rechazado'){
    const campos = [
      this.observaciones];

    for (const i of campos){
      if (this.contieneInyeccion(i)) {
        this.showMessage('error', 'Error en evaluación', 'No se permiten scripts o comandos en los campos de texto.');
        return;
      }
    }}

    if (this.decision === 'aprobado' && (!this.actaComite)){
      this.showMessage('error', 'Error en evaluación', 'El acta del comité es obligatoria para aprobar un evento');     
      return;
    }

    const idSecretaria = this.authService.getUserId();
    if (!idSecretaria) {
      this.showMessage('error', 'Error de sesión', 'No se encontró sesión activa');
      return;
    }

    console.log("🚀 Enviando evaluación:", {
      decision: this.decision,
      idSecretaria,
      tieneActa: !!this.actaComite,
      observaciones: this.observaciones
    });

    const formData = new FormData();
    formData.append('decision', this.decision);
    formData.append('idSecretaria', idSecretaria.toString());
    
    if (this.observaciones){
      formData.append('observaciones', this.observaciones);
    }    
    
    if (this.actaComite) {
      formData.append('actaComite', this.actaComite, this.actaComite.name);
    }

    const accion$ = this.decision === 'aprobado'
      ? this.evaluacionService.aprobar(this.eventoSeleccionado.codigo.toString(), formData)
      : this.evaluacionService.rechazar(this.eventoSeleccionado.codigo.toString(), formData);

    accion$.subscribe({
      next: (resp) => {
        console.log("✅ Evaluación exitosa:", resp);
        
        this.eventos = this.eventos.filter(e => e.codigo !== this.eventoSeleccionado.codigo);
        
        this.showMessage('success', '¡Evaluación Exitosa!', resp?.mensaje || 'El evento ha sido evaluado exitosamente');
        this.cerrarModalEvaluacion();
      },
      error: (err) => {
        console.error("❌ Error en evaluación:", err);
        const mensajeError = err?.error?.mensaje || err?.error?.message || 'No se pudo completar la acción';
        this.showMessage('error', 'Error', mensajeError);
      }
    });
  }
}