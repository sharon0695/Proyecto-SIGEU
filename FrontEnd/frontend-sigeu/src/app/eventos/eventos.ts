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
    codigo_lugar: '',
    nit_organizacion: ''
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
    aval?: File | null;
    nombre?: string;
    representante_legal?: string;
    ubicacion?: string;
    telefono?: string;
    sector_economico?: string;
    actividad_principal?: string;
  }> = [];
  selectedResponsables: Array<{ 
    id: number; 
    aval?: File | null;
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

  ngOnInit() {
    this.listar();
    this.cargarListas();
  }

  listar() {
    this.eventosService.listar().subscribe({
      next: (data) => (this.eventos = data || []),
      error: () => this.showMessage('error', 'Error de Carga', 'No fue posible cargar eventos'),
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
      if (org.aval && org.aval.type !== 'application/pdf') {
        return `El aval de la organización ${i + 1} debe ser un archivo PDF`;
      }
    }

    for (let i = 0; i < this.selectedResponsables.length; i++) {
      const resp = this.selectedResponsables[i];
      if (resp.aval && resp.aval.type !== 'application/pdf') {
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

    // Construir organizaciones
    const organizaciones: OrganizacionDTO[] = this.selectedOrganizaciones
      .filter(org => org.nit)
      .map(org => ({
        nit: org.nit,
        nombre: org.nombre,
        representante_legal: org.representante_legal,
        ubicacion: org.ubicacion,
        telefono: org.telefono,
        sector_economico: org.sector_economico,
        actividad_principal: org.actividad_principal,
        representante_alterno: org.tipo === 'alterno' ? org.alterno : undefined,
        certificado_participacion: org.aval ? org.aval.name : undefined
      }));

    // Construir responsables
    const responsables: ResponsableDTO[] = this.selectedResponsables
      .filter(resp => resp.id > 0)
      .map(resp => ({
        id_usuario: resp.id,
        documentoAval: resp.aval ? resp.aval.name : undefined,
        tipoAval: resp.tipoAval
      }));

    // Construir reservaciones
    const reservaciones: ReservacionDTO[] = this.selectedEspacios
      .filter(espacio => espacio)
      .map(espacio => ({
        codigo_espacio: espacio,
        hora_inicio: this.nuevoEvento.hora_inicio + ':00',
        hora_fin: this.nuevoEvento.hora_fin + ':00'
      }));

    const payload: EventoRegistroCompleto = {
      nombre: this.nuevoEvento.nombre,
      descripcion: this.nuevoEvento.descripcion || '',
      tipo: this.nuevoEvento.tipo || 'Academico',
      fecha: this.nuevoEvento.fecha,
      hora_inicio: this.nuevoEvento.hora_inicio + ':00',
      hora_fin: this.nuevoEvento.hora_fin + ':00',
      id_usuario_registra: userId,
      organizaciones,
      responsables,
      reservaciones
    };

    this.eventosService.registrar(payload).subscribe({
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

    // Construir organizaciones - manejar valores null
    const organizaciones: OrganizacionDTO[] = this.selectedOrganizaciones
      .filter(org => org.nit)
      .map(org => {
        const orgDTO: OrganizacionDTO = {
          nit: org.nit,
          nombre: org.nombre,
          representante_legal: org.representante_legal,
          ubicacion: org.ubicacion,
          telefono: org.telefono,
          sector_economico: org.sector_economico,
          actividad_principal: org.actividad_principal,
          representante_alterno: org.tipo === 'alterno' ? org.alterno : undefined,
          certificado_participacion: org.aval ? org.aval.name : undefined
        };

        // Manejar certificado_participacion (convertir null a undefined)
        if (org.aval) {
          orgDTO.certificado_participacion = org.aval.name;
        } else if (org.aval) {
          orgDTO.certificado_participacion = org.aval;
        }
        // Si no hay ninguno, queda como undefined

        return orgDTO;
      });

    // Construir responsables - manejar valores null
    const responsables: ResponsableDTO[] = this.selectedResponsables
      .filter(resp => resp.id > 0)
      .map(resp => {
        const respDTO: ResponsableDTO = {
          id_usuario: resp.id,
          documentoAval: resp.aval ? resp.aval.name : undefined,
          tipoAval: resp.tipoAval
        };

        // Manejar documentoAval (convertir null a undefined)
        if (resp.aval) {
          respDTO.documentoAval = resp.aval.name;
        } else if (resp.aval) {
          respDTO.documentoAval = resp.aval;
        }
        // Si no hay ninguno, queda como undefined

        return respDTO;
      });

    // Construir reservaciones
    const reservaciones: ReservacionDTO[] = this.selectedEspacios
      .filter(espacio => espacio)
      .map(espacio => ({
        codigo_espacio: espacio,
        hora_inicio: this.nuevoEvento.hora_inicio + ':00',
        hora_fin: this.nuevoEvento.hora_fin + ':00'
      }));

    const payload: EventoEdicionCompleto = {
      codigo: this.editCodigo,
      nombre: this.nuevoEvento.nombre,
      descripcion: this.nuevoEvento.descripcion || '',
      tipo: this.nuevoEvento.tipo || 'Academico',
      fecha: this.nuevoEvento.fecha,
      hora_inicio: this.nuevoEvento.hora_inicio + ':00',
      hora_fin: this.nuevoEvento.hora_fin + ':00',
      id_usuario_registra: userId,
      organizaciones,
      responsables,
      reservaciones
    };

    this.eventosService.editar(payload).subscribe({
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
        this.selectedResponsables.push({ id: userId, aval: null });
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
    }, 5000);
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
      aval: null 
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
    
    this.selectedResponsables.push({ id: 0, aval: null }); 
  }

  removeResponsable(i: number) { 
    this.selectedResponsables.splice(i, 1); 
  }

  showOrgInline = false;
  orgInline: any = { nit: '', nombre: '', representante_legal: '', telefono: '', ubicacion: '', sector_economico: '', actividad_principal: '' };

  openOrgInlineModal() { 
    this.orgInline = {}; 
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
    const body = { ...this.orgInline, usuario: { identificacion: idUsuario } };
    this.organizacionesService.registrar(body).subscribe({
      next: () => { 
        this.selectedOrganizaciones.push({ 
          nit: this.orgInline.nit, 
          tipo: 'legal', 
          alterno: '', 
          aval: null,
          nombre: this.orgInline.nombre,
          representante_legal: this.orgInline.representante_legal,
          ubicacion: this.orgInline.ubicacion,
          telefono: this.orgInline.telefono,
          sector_economico: this.orgInline.sector_economico,
          actividad_principal: this.orgInline.actividad_principal
        }); 
        this.showOrgInline = false; 
        this.showMessage('success', '¡Éxito!', 'Organización creada y agregada al evento');
        this.cargarListas(); // Recargar lista de organizaciones
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

          // Cargar organizaciones
          this.selectedOrganizaciones = (evento.organizaciones || []).map((org: any) => ({
            nit: org.nit,
            nombre: org.nombre,
            representante_legal: org.representante_legal,
            ubicacion: org.ubicacion,
            telefono: org.telefono,
            sector_economico: org.sector_economico,
            actividad_principal: org.actividad_principal,
            tipo: org.representante_alterno ? 'alterno' : 'legal',
            alterno: org.representante_alterno || '',
            certificado_participacion: org.certificado_participacion
          }));

          // Cargar responsables
          this.selectedResponsables = (evento.responsables || []).map((resp: any) => ({
            id: resp.id_usuario,
            tipoAval: resp.tipoAval,
            documentoAval: resp.documentoAval
          }));

          this.openModal();
        },
        error: (err) => {
          console.error('Error al cargar evento para edición:', err);
          this.showMessage('error', 'Error', 'No se pudo cargar los datos del evento para editar');
        }
      });
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
    this.selectedOrganizaciones[i].aval = file;
  }

  onRespAvalChange(event: Event, i: number) {
    const file = (event.target as HTMLInputElement).files?.[0] || null;
    if (file && file.type !== 'application/pdf') {
      this.showMessage('error', 'Formato Incorrecto', 'El aval del responsable debe ser un archivo PDF');
      (event.target as HTMLInputElement).value = '';
      return;
    }
    this.selectedResponsables[i].aval = file;
  }
}