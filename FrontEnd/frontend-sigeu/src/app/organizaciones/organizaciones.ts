import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrganizacionesService } from '../services/organizaciones.service';
import { AuthService } from '../services/auth.service';
import { RouterLink } from '@angular/router';
import { Notificaciones } from '../notificaciones/notificaciones';

@Component({
  selector: 'app-organizaciones',
  imports: [CommonModule, FormsModule, RouterLink, Notificaciones],
  templateUrl: './organizaciones.html',
  styleUrl: './organizaciones.css'
})
export class Organizaciones {
  organizaciones: any[] = [];
  organizacionesTodas: any[] = [];
  nombreBusqueda = '';
  mensaje = '';
  newOrg: any = {
    nit: '',
    nombre: '',
    representante_legal: '',
    ubicacion: '',
    telefono: '',
    sector_economico: '',
    actividad_principal: ''
  };

  // Modal de mensajes
  showMessageModal = false;
  messageType: 'success' | 'error' = 'success';
  messageText = '';
  messageTitle = '';
  editMode: boolean = false;
  originalNit: string | null = null;


  constructor(private orgs: OrganizacionesService, private auth: AuthService) {}
  showModal = false;
  showViewModal = false;
  viewOrg: any;

  paginaActual = 1;
  elementosPorPagina = 8; 

  get totalPaginas(): number {
    return Math.ceil(this.organizaciones.length / this.elementosPorPagina);
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
  }

  listar() {
    this.orgs.listar().subscribe({
      next: (data) => { this.organizaciones = data || []; this.organizacionesTodas = this.organizaciones.slice(); },
      error: () => this.showMessage('error', 'Error de Carga', 'No fue posible cargar organizaciones'),
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

  private containsInvalidChars(text: string): boolean {
    // Expresión Regular: Permite letras (a-z, A-Z), números (0-9), espacios, guiones (-) y puntos (.).
    // Esto es un estándar común para nombres y documentos.
    const regex = /[^a-zA-Z0-9\s\.\-]/; 
    return regex.test(text);
  }
  
  buscar() {
    if (!this.nombreBusqueda) { this.organizaciones = this.organizacionesTodas.slice(); return; }
    const term = this.nombreBusqueda.trim().toLowerCase();
    this.organizaciones = this.organizacionesTodas.filter(o => (o?.nombre || '').toLowerCase() === term);
    if (!this.organizaciones.length) {
      this.organizaciones = this.organizacionesTodas.filter(o => (o?.nombre || '').toLowerCase().includes(term));
    }
    if (!this.organizaciones.length) {
      this.showMessage('error', 'Sin Resultados', 'No se encontraron organizaciones con ese nombre');
    }
  }

  registrarNueva(org: any) {
    const idUsuario = this.auth.getUserId();
    if (!idUsuario) { this.mensaje = 'Debes iniciar sesión para registrar organizaciones'; return; }
    const body = { ...org, usuario: { identificacion: idUsuario } };
    this.orgs.registrar(body).subscribe({ next: () => this.listar(), error: () => (this.mensaje = 'No fue posible registrar la organización') });
  }  

  onSubmitRegistrarOrg(event: Event) {
    event.preventDefault();

    const idUsuario = this.auth.getUserId();
    if (!idUsuario) {
      this.showMessage('error', 'Error de Sesión', 'Debes iniciar sesión');
      return;
    }

    const body = { ...this.newOrg, usuario: { identificacion: idUsuario } };

    if (!body.nit?.trim() || !body.nombre?.trim() || !body.representante_legal?.trim() ||
        !body.ubicacion?.trim() || !body.sector_economico?.trim() || !body.actividad_principal?.trim()) {
      this.showMessage('error', 'Campos incompletos', 'Por favor completa todos los campos obligatorios.');
      return;
    }
    
    if (body.nombre.length > 30) {
        this.showMessage('error', 'Longitud excedida', 'El Nombre de la Organización no puede exceder los 30 caracteres.');
        return;
    }
    if (body.representante_legal.length > 30) {
        this.showMessage('error', 'Longitud excedida', 'El Representante Legal no puede exceder los 30 caracteres.');
        return;
    }
    if (body.ubicacion.length > 40) {
        this.showMessage('error', 'Longitud excedida', 'La Ubicación no puede exceder los 40 caracteres.');
        return;
    }
    if (body.sector_economico.length > 20) {
        this.showMessage('error', 'Longitud excedida', 'El Sector Económico no puede exceder los 20 caracteres.');
        return;
    }
    if (body.actividad_principal.length > 100) { 
        this.showMessage('error', 'Longitud excedida', 'La Descripción (Actividad Principal) no puede exceder los 100 caracteres.');
        return;
    }

    // A. Validación de caracteres especiales
    if (this.containsInvalidChars(body.nombre) || 
        this.containsInvalidChars(body.nit) || 
        this.containsInvalidChars(body.representante_legal)) {
      this.showMessage('error', 'Caracteres inválidos', 'Los campos Nombre, NIT y Representante Legal solo pueden contener letras, números, espacios, puntos (.) y guiones (-).');
      return;
    }

    // B. Validación de 9 caracteres del NIT (Manteniendo la restricción anterior)
    if (!this.editMode && body.nit.length > 9) {
      this.showMessage('error', 'NIT inválido', 'El NIT no puede tener más de 9 caracteres.');
      return;
    }

    // Esta validación solo se aplica al crear (ya que en modo edición el NIT es readonly)
    if (!this.editMode && body.nit.length > 9) {
      this.showMessage('error', 'NIT inválido', 'El NIT no puede tener más de 9 caracteres.');
      return;
    }
    
    if (!body.telefono || !/^\d+$/.test(body.telefono)) {
      this.showMessage('error', 'Teléfono inválido', 'El teléfono debe contener solo números y no puede estar vacío.');
      return;
    }

    if (this.editMode) {
      this.orgs.editar(this.originalNit!, idUsuario, this.newOrg).subscribe({
        next: () => {
          this.showMessage('success', '¡Éxito!', 'Organización actualizada correctamente');
          this.listar();
          this.showModal = false;
        },
        error: (err) => {
          this.showMessage('error', 'Error al Actualizar', err?.error?.mensaje || 'No se pudo actualizar la organización');
        }
      });
    } else {
      this.orgs.registrar(body).subscribe({
        next: () => {
          this.showMessage('success', '¡Éxito!', 'Organización registrada correctamente');
          this.listar();
          this.showModal = false;
        },
        error: (err) => {
          const mensajeError = err?.error?.mensaje;

          if (mensajeError?.includes('Ya existe una organización con ese NIT')) {
            this.showMessage('error', 'NIT duplicado', 'El NIT ingresado ya está registrado en el sistema.');
          } else if (mensajeError?.includes('Hay campos obligatorios vacíos')) {
            this.showMessage('error', 'Campos incompletos', 'Por favor completa todos los campos obligatorios.');
          } else if (mensajeError?.includes('El teléfono solo debe contener números')) {
            this.showMessage('error', 'Teléfono inválido', 'El teléfono debe contener solo números.');
          } else {
            this.showMessage('error', 'Error al Registrar', mensajeError || 'No se pudo registrar la organización');
          }
        }
      });
    }
  }
  openModal() { this.showModal = true; }
  closeModal() { this.showModal = false; }

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

  onEliminar(org: any) {
    if (!org?.nit) { 
      this.showMessage('error', 'Error de Validación', 'Organización inválida'); 
      return; 
    }
    const confirmacion = confirm(`¿Eliminar la organización ${org.nombre || org.nit}?`);
    if (!confirmacion) { 
      this.showMessage('error', 'Operación Cancelada', 'La eliminación fue cancelada'); 
      return; 
    }
    const idUsuario = this.auth.getUserId();
    if (!idUsuario) { 
      this.showMessage('error', 'Error de Sesión', 'Debes iniciar sesión para eliminar organizaciones'); 
      return; 
    }
    this.orgs.eliminar(org.nit, idUsuario).subscribe({
      next: () => {
        this.showMessage('success', '¡Éxito!', 'Organización eliminada correctamente');
        this.listar();
      },
      error: (err) => { 
        const msg = err?.error?.mensaje || err?.error || 'No se pudo eliminar la organización'; 
        this.showMessage('error', 'Error al Eliminar', msg); 
      }
    });
  }

  onVisualizar(org: any) { this.viewOrg = { ...org }; this.showViewModal = true; }
  closeViewModal() { this.showViewModal = false; this.viewOrg = null; this.editMode = false; this.originalNit = null;
    this.newOrg = {
      nit: '',
      nombre: '',
      representante_legal: '',
      ubicacion: '',
      telefono: '',
      sector_economico: '',
      actividad_principal: ''
    };
  }

  onEditar(org: any) {
  const idUsuario = this.auth.getUserId();
  this.editMode = true;
  if (!idUsuario) { 
    this.showMessage('error', 'Error de Sesión', 'Debes iniciar sesión'); 
    return; 
  }
  if (org?.usuario?.identificacion && org.usuario.identificacion !== idUsuario) {
    this.showMessage('error', 'Sin Permisos', 'No tienes permisos para editar esta organización');
    return;
  }
  this.newOrg = JSON.parse(JSON.stringify(org));  
  
  this.originalNit = org.nit;
  this.showModal = true;
}
}
