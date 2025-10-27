import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { EventosService, EventoRegistroCompleto, EventoEdicionCompleto, OrganizacionDTO, ResponsableDTO, ReservacionDTO } from '../services/eventos.service';
import { EspacioService } from '../services/espacio.service';
import { OrganizacionesService } from '../services/organizaciones.service';
import { Api } from '../services/usuarios.service';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-eventos',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './eventos.html',
  styleUrl: './eventos.css'
})
export class Eventos {
  eventos: any[] = [];
  mensaje = '';
  nuevoEvento: any = {
    nombre: '',
    descripcion: '',
    tipo: '',
    fecha: '',
    hora_inicio: '',
    hora_fin: '',
    idUsuarioRegistra: ''
  };
  showModal = false;
  editMode = false;
  editCodigo: number | null = null;

  // Modal de mensajes
  showMessageModal = false;
  messageType: 'success' | 'error' = 'success';
  messageText = '';
  messageTitle = '';
  
  espaciosListado: Array<{ codigo: string; nombre?: string }> = [];
  organizacionesListado: Array<{ nit: string; nombre?: string }> = [];
  usuariosListado: Array<{ identificacion: number; nombre?: string; apellido?: string }> = [];

  selectedEspacios: string[] = [];
  selectedOrganizaciones: Array<{ 
    nit: string; 
    tipo: 'legal' | 'alterno'; 
    alterno?: string; 
    avalNuevo?: File | null;
    certificadoExistente?: string;    
  }> = [];

  selectedResponsables: Array<{ 
    id: number; 
    avalNuevo?: File | null;
    documentoExistente?: string;       
    tipoAval?: string;
  }> = [];

  constructor(
    private eventosService: EventosService,
    private espacioService: EspacioService,
    private organizacionesService: OrganizacionesService,
    private api: Api,
    private auth: AuthService,
    private router: Router
  ) {}

  filtroTipo: string = 'nombre';
  valorFiltro: string = '';
  eventosFiltrados: any[] = [];

  paginaActual = 1;
  elementosPorPagina = 8; 


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
    this.listar();
    this.cargarListas();
  }

  listar() {
    const idUsuarioActivo = this.auth.getUserId();
    if (idUsuarioActivo === null) {
      this.showMessage('error', 'Sesión no válida', 'No se encontró un usuario activo');
      return;
    }
    this.eventosService.listar(idUsuarioActivo).subscribe({
      next: (data) => {
        this.eventos = data || [];
        this.eventosFiltrados = [...this.eventos];
      },
      error: () =>
        this.showMessage('error', 'Error de Carga', 'No fue posible cargar eventos'),
    });
  }

  private cargarListas() {
    this.espacioService.listar().subscribe({
      next: (data) => {
        this.espaciosListado = (data || []).map((e: any) => ({ codigo: e.codigo, nombre: e.nombre }));
      }
    });
    this.organizacionesService.listar().subscribe({
      next: (data) => {
        this.organizacionesListado = (data || []).map((o: any) => ({ nit: o.nit, nombre: o.nombre }));
      }
    });
    this.api.getUsuarios().subscribe({
      next: (data) => {
        this.usuariosListado = (data || [])
          .filter((u: any) => {
            const rol = (u.rol || '').toString().toLowerCase();
            return rol === 'docente' || rol === 'estudiante';
          })
          .map((u: any) => ({ identificacion: u.identificacion, nombre: u.nombre, apellido: u.apellido }));
      }
    });
  }

  private validarFormulario(): string | null {
    // Validación de campos básicos
    if (!this.nuevoEvento.nombre?.trim()) {
      return 'El nombre del evento es obligatorio';
    }
    
    if (!this.nuevoEvento.fecha) {
      return 'La fecha del evento es obligatoria';
    }
    
    if (!this.nuevoEvento.hora_inicio) {
      return 'La hora de inicio es obligatoria';
    }
    
    if (!this.nuevoEvento.hora_fin) {
      return 'La hora de fin es obligatoria';
    }
    
    if (!this.nuevoEvento.tipo) {
      return 'El tipo de evento es obligatorio';
    }

    // Validar fecha no sea anterior a hoy
    const fechaEvento = new Date(this.nuevoEvento.fecha + 'T00:00:00');
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    
    if (fechaEvento < hoy) {
      return 'La fecha del evento debe ser igual o posterior a la fecha actual';
    }

    // Validar horas
    const horaInicio = this.nuevoEvento.hora_inicio;
    const horaFin = this.nuevoEvento.hora_fin;

    if (horaInicio === horaFin) {
      return 'La hora de inicio y la hora de fin no pueden ser iguales';
    }

    if (horaInicio >= horaFin) {
      return 'La hora de fin debe ser posterior a la hora de inicio';
    }

    // Validar espacios
    if (!this.selectedEspacios.length || this.selectedEspacios.every(e => !e)) {
      return 'Debe seleccionar al menos un espacio';
    }

    // Validar responsables
    if (!this.selectedResponsables.length || this.selectedResponsables.every(r => r.id === 0)) {
      return 'Debe haber al menos un responsable';
    }

    // Validar archivos PDF
    for (let i = 0; i < this.selectedOrganizaciones.length; i++) {
      const org = this.selectedOrganizaciones[i];
      if (org.avalNuevo && org.avalNuevo.type !== 'application/pdf') {
        return `El aval de la organización ${i + 1} debe ser un archivo PDF`;
      }
    }

    for (let i = 0; i < this.selectedResponsables.length; i++) {
      const resp = this.selectedResponsables[i];
      if (resp.avalNuevo && resp.avalNuevo.type !== 'application/pdf') {
        return `El aval del responsable ${i + 1} debe ser un archivo PDF`;
      }
    }

    return null;
  }

  crear() {
    // Validar formulario
    const errorValidacion = this.validarFormulario();
    if (errorValidacion) {
      this.showMessage('error', 'Error de Validación', errorValidacion);
      return;
    }

    const userId = this.auth.getUserId();
    if (!userId) {
      this.showMessage('error', 'Error de Sesión', 'Debes iniciar sesión para crear eventos');
      return;
    }

    // Crear FormData en lugar de JSON
    const formData = new FormData();

    // Agregar datos básicos del evento
    formData.append('nombre', this.nuevoEvento.nombre);
    formData.append('descripcion', this.nuevoEvento.descripcion || '');
    formData.append('tipo', this.nuevoEvento.tipo || 'Academico');
    formData.append('fecha', this.nuevoEvento.fecha);
    formData.append('hora_inicio', this.nuevoEvento.hora_inicio + ':00');
    formData.append('hora_fin', this.nuevoEvento.hora_fin + ':00');
    formData.append('id_usuario_registra', userId.toString());

    // Agregar organizaciones
    this.selectedOrganizaciones
    .filter(org => org.nit)
    .forEach((org, index) => {
      formData.append(`colaboraciones[${index}].nit`, org.nit);
      formData.append(`colaboraciones[${index}].representante_alterno`, org.tipo === 'alterno' ? (org.alterno || '') : '');
      
      // Solo enviar archivo nuevo si se seleccionó
      if (org.avalNuevo) {
        formData.append(`colaboraciones[${index}].certificado_participacion`, org.avalNuevo, org.avalNuevo.name);
      } else if (org.certificadoExistente) {
        // Mantener archivo existente si no se sube uno nuevo
        formData.append(`colaboraciones[${index}].certificado_existente`, org.certificadoExistente);
      }
    });


    // Agregar responsables
    this.selectedResponsables
      .filter(resp => resp.id > 0)
      .forEach((resp, index) => {
        formData.append(`responsables[${index}].id_usuario`, resp.id.toString());
        formData.append(`responsables[${index}].tipoAval`, resp.tipoAval || '');
        
        if (resp.avalNuevo) {
          formData.append(`responsables[${index}].documentoAval`, resp.avalNuevo, resp.avalNuevo.name);
        }
      });

    // Agregar reservaciones
    this.selectedEspacios
      .filter(espacio => espacio)
      .forEach((espacio, index) => {
        formData.append(`reservaciones[${index}].codigo_espacio`, espacio);
        formData.append(`reservaciones[${index}].hora_inicio`, this.nuevoEvento.hora_inicio + ':00');
        formData.append(`reservaciones[${index}].hora_fin`, this.nuevoEvento.hora_fin + ':00');
      });

    // Usar el nuevo método que acepta FormData
    this.eventosService.registrar(formData).subscribe({
      next: (response) => {
        this.showMessage('success', '¡Registro Exitoso!', response?.mensaje || 'El evento ha sido registrado exitosamente');
        this.listar();
        this.closeModal();
      },
      error: (err) => {
        console.error('Error al registrar evento:', err);
        const mensajeError = err?.error?.mensaje || err?.error?.message || 'No fue posible registrar el evento';
        this.showMessage('error', 'Error al Registrar', mensajeError);
      }
    });
  }

  editar() {
    // Validar formulario
    const errorValidacion = this.validarFormulario();
    if (errorValidacion) {
      this.showMessage('error', 'Error de Validación', errorValidacion);
      return;
    }

    const userId = this.auth.getUserId();
    if (!userId) {
      this.showMessage('error', 'Error de Sesión', 'Debes iniciar sesión para editar eventos');
      return;
    }

    if (!this.editCodigo) {
      this.showMessage('error', 'Error', 'No se puede editar el evento sin código');
      return;
    }

    // Crear FormData para edición
    const formData = new FormData();

    // Agregar datos básicos del evento
    formData.append('codigo', this.editCodigo.toString());
    formData.append('nombre', this.nuevoEvento.nombre);
    formData.append('descripcion', this.nuevoEvento.descripcion || '');
    formData.append('tipo', this.nuevoEvento.tipo || 'Academico');
    formData.append('fecha', this.nuevoEvento.fecha);
    formData.append('hora_inicio', this.nuevoEvento.hora_inicio + ':00');
    formData.append('hora_fin', this.nuevoEvento.hora_fin + ':00');
    formData.append('id_usuario_registra', userId.toString());

    // Agregar organizaciones
    this.selectedOrganizaciones
      .filter(org => org.nit)
      .forEach((org, index) => {
        formData.append(`colaboraciones[${index}].nit`, org.nit);
        formData.append(`colaboraciones[${index}].representante_alterno`, org.tipo === 'alterno' ? (org.alterno || '') : '');
        
        // Solo enviar archivo nuevo si se seleccionó
        if (org.avalNuevo) {
          formData.append(`colaboraciones[${index}].certificado_participacion`, org.avalNuevo, org.avalNuevo.name);
        } else if (org.certificadoExistente) {
          // Mantener archivo existente si no se sube uno nuevo
          formData.append(`colaboraciones[${index}].certificado_existente`, org.certificadoExistente);
        }
      });


    // Agregar responsables
    this.selectedResponsables
      .filter(resp => resp.id > 0)
      .forEach((resp, index) => {
        formData.append(`responsables[${index}].id_usuario`, resp.id.toString());
        formData.append(`responsables[${index}].tipoAval`, resp.tipoAval || '');
        
        // Solo enviar archivo nuevo si se seleccionó
        if (resp.avalNuevo) {
          formData.append(`responsables[${index}].documentoAval`, resp.avalNuevo, resp.avalNuevo.name);
        } else if (resp.documentoExistente) {
          // Mantener archivo existente si no se sube uno nuevo
          formData.append(`responsables[${index}].documento_existente`, resp.documentoExistente);
        }
      });

    // Agregar reservaciones
    this.selectedEspacios
      .filter(espacio => espacio)
      .forEach((espacio, index) => {
        formData.append(`reservaciones[${index}].codigo_espacio`, espacio);
        formData.append(`reservaciones[${index}].hora_inicio`, this.nuevoEvento.hora_inicio + ':00');
        formData.append(`reservaciones[${index}].hora_fin`, this.nuevoEvento.hora_fin + ':00');
      });

    // Usar el nuevo método que acepta FormData
    this.eventosService.editar(formData).subscribe({
      next: (response) => {
        this.showMessage('success', '¡Edición Exitosa!', response?.mensaje || 'El evento ha sido actualizado exitosamente');
        this.listar();
        this.closeModal();
      },
      error: (err) => {
        console.error('Error al editar evento:', err);
        const mensajeError = err?.error?.mensaje || err?.error?.message || 'No fue posible actualizar el evento';
        this.showMessage('error', 'Error al Editar', mensajeError);
      }
    });
  }

  // Método para mostrar archivos existentes en el HTML
  getFileName(filePath: string | undefined): string {
    if (!filePath) return 'Ningún archivo seleccionado';
    
    // Extraer solo el nombre del archivo de la ruta completa
    const parts = filePath.split('/');
    return parts[parts.length - 1] || 'Archivo existente';
  }

  // Método para ver archivo existente
  verArchivo(tipo: 'organizaciones' | 'responsables', filePath: string) {
    if (!filePath) {
      this.showMessage('error', 'Archivo no disponible', 'No hay archivo para mostrar');
      return;
    }

    const url = this.eventosService.getFileViewUrl(tipo, filePath);
    
    window.open(url, '_blank');
  }

  // Método para descargar archivo existente
  descargarArchivo(tipo: 'organizaciones' | 'responsables', filePath: string) {
    if (!filePath) {
      this.showMessage('error', 'Archivo no disponible', 'No hay archivo para descargar');
      return;
    }

    const url = this.eventosService.getFileDownloadUrl(tipo, filePath);
    
    window.open(url, '_blank');
  }
  getNewFileName(file: File | null | undefined): string {
    if (!file) return 'Ningún archivo seleccionado';
    return file.name;
  }

  onSubmitCrearEvento(event: Event) {
    event.preventDefault();
    
    if (this.editMode && this.editCodigo != null) {
      this.editar();
    } else {
      this.crear();
    }
  }

  openModal() {
    this.showModal = true;
    this.mensaje = '';
    if (!this.editMode) {
      this.limpiarFormulario();
      
      // Agregar el usuario actual como responsable por defecto
      const userId = this.auth.getUserId();
      if (userId) {
        this.selectedResponsables.push({ id: userId, avalNuevo: null });
      }
    }
  }

  closeModal() { 
    this.showModal = false; 
    this.editMode = false; 
    this.editCodigo = null; 
    this.mensaje = '';
    this.limpiarFormulario();
  }

  private limpiarFormulario() {
    this.nuevoEvento = {
      nombre: '',
      descripcion: '',
      tipo: 'Academico',
      fecha: '',
      hora_inicio: '',
      hora_fin: '',
      codigo_lugar: '',
      nit_organizacion: ''
    };
    this.selectedEspacios = [];
    this.selectedOrganizaciones = [];
    this.selectedResponsables = [];
  }

  // Métodos para el modal de mensajes
  showMessage(type: 'success' | 'error', title: string, message: string) {
    this.messageType = type;
    this.messageTitle = title;
    this.messageText = message;
    this.showMessageModal = true;
    
    // Auto cerrar después de 5 segundos
    setTimeout(() => {
      this.closeMessageModal();
    }, 8000);
  }

  closeMessageModal() {
    this.showMessageModal = false;
    this.messageText = '';
    this.messageTitle = '';
  }

  addEspacio() { 
    const availableEspacios = this.espaciosListado.filter(e => !this.selectedEspacios.includes(e.codigo));
    
    if (availableEspacios.length === 0) {
      this.showMessage('error', 'Sin Espacios Disponibles', 'No hay más espacios disponibles para agregar');
      return;
    }
    
    this.selectedEspacios.push(''); 
  }

  removeEspacio(i: number) { 
    this.selectedEspacios.splice(i, 1); 
  }

  addOrganizacion() { 
    const existingNits = this.selectedOrganizaciones.map(o => o.nit).filter(n => n);
    const availableOrgs = this.organizacionesListado.filter(o => !existingNits.includes(o.nit));
    
    if (availableOrgs.length === 0) {
      this.showMessage('error', 'Sin Organizaciones Disponibles', 'No hay más organizaciones disponibles para agregar');
      return;
    }
    
    this.selectedOrganizaciones.push({ 
      nit: '', 
      tipo: 'legal', 
      alterno: '', 
      avalNuevo: null 
    }); 
  }

  removeOrganizacion(i: number) { 
    this.selectedOrganizaciones.splice(i, 1); 
  }

  addResponsable() { 
    const existingIds = this.selectedResponsables.map(r => r.id).filter(id => id > 0);
    const availableUsers = this.usuariosListado.filter(u => !existingIds.includes(u.identificacion));
    
    if (availableUsers.length === 0) {
      this.showMessage('error', 'Sin Usuarios Disponibles', 'No hay más usuarios disponibles para agregar como responsables');
      return;
    }
    
    this.selectedResponsables.push({ id: 0, avalNuevo: null }); 
  }

  removeResponsable(i: number) { 
    this.selectedResponsables.splice(i, 1); 
  }

  showOrgInline = false;
  orgInline: any = { nit: '', nombre: '', representante_legal: '', telefono: '', ubicacion: '', sector_economico: '', actividad_principal: '' };

  openOrgInlineModal() { 
    this.orgInline = { 
      nit: '', 
      nombre: '', 
      representante_legal: '', 
      telefono: '', 
      ubicacion: '', 
      sector_economico: '', 
      actividad_principal: '' 
    }; 
    this.showOrgInline = true; 
  }

  closeOrgInlineModal() { 
    this.showOrgInline = false; 
  }

  onSubmitOrgInline(event: Event) {
    event.preventDefault();
    const idUsuario = this.auth.getUserId(); 
    if (!idUsuario) { 
      this.showMessage('error', 'Error de Sesión', 'Debes iniciar sesión'); 
      return; 
    }
    
    const body = { 
      ...this.orgInline, 
      usuario: { identificacion: idUsuario } 
    };
    
    this.organizacionesService.registrar(body).subscribe({
      next: () => { 
        // Solo agregar el NIT a las colaboraciones, no toda la información
        this.selectedOrganizaciones.push({ 
          nit: this.orgInline.nit, 
          tipo: 'legal', 
          alterno: '', 
          avalNuevo: null
          // Eliminamos todos los demás campos que no necesitamos
        }); 
        
        this.showOrgInline = false; 
        this.showMessage('success', '¡Éxito!', 'Organización creada y agregada al evento');
        this.cargarListas(); // Recargar lista de organizaciones
        
        // Limpiar el formulario
        this.orgInline = { 
          nit: '', 
          nombre: '', 
          representante_legal: '', 
          telefono: '', 
          ubicacion: '', 
          sector_economico: '', 
          actividad_principal: '' 
        };
      },
      error: (err) => { 
        const mensajeError = err?.error?.mensaje || err?.error?.message || 'No se pudo crear organización';
        this.showMessage('error', 'Error al Crear Organización', mensajeError); 
      }
    });
  }

  nuevaOrganizacion() {
    this.router.navigate(['/organizaciones'], { queryParams: { nuevo: '1' } });
  }

  openEdit(e: any) {
    this.editMode = true;
    this.editCodigo = e?.codigo ?? null;   
    if (this.editCodigo != null) {
      this.eventosService.obtenerDetalles(this.editCodigo).subscribe({
        next: (evento) => {
          // Cargar datos básicos del evento
          this.nuevoEvento = {
            nombre: evento.nombre || '',
            descripcion: evento.descripcion || '',
            tipo: evento.tipo || '',
            fecha: evento.fecha || '',
            hora_inicio: evento.hora_inicio ? evento.hora_inicio.substring(0, 5) : '',
            hora_fin: evento.hora_fin ? evento.hora_fin.substring(0, 5) : ''
          };

          // Cargar espacios
          this.selectedEspacios = (evento.reservaciones || []).map((r: any) => r.codigo_espacio);

          // Cargar organizaciones - solo información básica
          this.selectedOrganizaciones = (evento.organizaciones || []).map((org: any) => ({
            nit: org.nit,
            tipo: org.representante_alterno ? 'alterno' : 'legal',
            alterno: org.representante_alterno || '',
            certificadoExistente: org.certificado_participacion, // Solo para referencia
            avalNuevo: null
          }));

          // Cargar responsables - solo información básica
          this.selectedResponsables = (evento.responsables || []).map((resp: any) => ({
            id: resp.id_usuario,
            tipoAval: resp.tipoAval || undefined,
            documentoExistente: resp.documentoAval, // Solo para referencia
            avalNuevo: null
          }));

          this.openModal();
        },
        error: (err) => {          
          const mensajeError = err?.error?.mensaje || err?.error?.message || 'No fue posible registrar el evento';
          this.showMessage('error', 'Error al Registrar', mensajeError);
     } });
    }
  }

  onOrgTipoChange(i: number) {
    if (this.selectedOrganizaciones[i].tipo !== 'alterno') {
      this.selectedOrganizaciones[i].alterno = '';
    }
  }

  onOrgAvalChange(event: Event, i: number) {
    const file = (event.target as HTMLInputElement).files?.[0] || null;
    if (file && file.type !== 'application/pdf') {
      this.showMessage('error', 'Formato Incorrecto', 'El aval de organización debe ser un archivo PDF');
      (event.target as HTMLInputElement).value = '';
      return;
    }
    this.selectedOrganizaciones[i].avalNuevo = file;
  }

  onRespAvalChange(event: Event, i: number) {
    const file = (event.target as HTMLInputElement).files?.[0] || null;
    if (file && file.type !== 'application/pdf') {
      this.showMessage('error', 'Formato Incorrecto', 'El aval del responsable debe ser un archivo PDF');
      (event.target as HTMLInputElement).value = '';
      return;
    }
    this.selectedResponsables[i].avalNuevo = file;
  }

  eliminarEvento(codigo: number) {
    const confirmacion = confirm('¿Estás seguro de que deseas eliminar este evento?');

    if (!confirmacion) return;

    this.eventosService.eliminarEvento(codigo).subscribe({
      next: () => {
        this.showMessage('success', '¡Éxito!', 'Evento eliminado correctamente');
        this.listar(); 
      },
      error: (err) => {
        const mensajeError = err?.error?.mensaje || err?.error?.message || 'No se pudo eliminar el evento';
        this.showMessage('error', 'Error al Eliminar Evento', mensajeError); 
      }
    });
  }

  filtrarEventos() {
    const valor = this.valorFiltro.toLowerCase().trim();

    if (!valor) {
      this.eventosFiltrados = [...this.eventos];
      return;
    }

    this.eventosFiltrados = this.eventos.filter(e => {
      const nombre = e.nombre?.toLowerCase() || '';
      const fecha = e.fecha?.toString().toLowerCase() || '';
      const estado = e.estado?.toLowerCase() || '';

      if (this.filtroTipo === 'nombre') return nombre.includes(valor);
      if (this.filtroTipo === 'fecha') return fecha.includes(valor);
      if (this.filtroTipo === 'estado') return estado.includes(valor);
      return false;
    });
  }

  
  limpiarFiltro(): void {
    this.valorFiltro = '';
    this.listar();
  }

  enviarEvento(codigo: number) {
    if (!confirm('¿Seguro que deseas enviar este evento a revisión, luego de confirmar no puede modificar el evento?')) {
      return;
    }

    this.eventosService.enviarEvento(codigo).subscribe({
      next: (response) => {
        this.showMessage('success', 'Evento enviado', response.mensaje || 'El evento fue enviado correctamente.');
        this.listar(); // 🔄 vuelve a cargar la lista para actualizar el estado
      },
      error: (error) => {
        const errorMsg = error.error?.message || error.error || 'Error al enviar el evento.';
        this.showMessage('error', 'Error de envío', errorMsg);
      }
    });
  }
}
