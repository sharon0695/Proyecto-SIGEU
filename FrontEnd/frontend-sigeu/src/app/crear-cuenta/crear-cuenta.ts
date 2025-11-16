import { Component, OnInit } from '@angular/core';
import { Api, UsuarioRegistroDto } from '../services/usuarios.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProgramasService } from '../services/programas.service';
import { UnidadService } from '../services/unidad.service';
import { FacultadService } from '../services/facultad.service';

@Component({
  selector: 'app-crear-cuenta',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './crear-cuenta.html',
  styleUrl: './crear-cuenta.css'
})
export class CrearCuenta implements OnInit {
  usuario = {
    identificacion: '',
    nombre: '',
    apellido: '',
    correo: '',
    contrasena: '',
    confirmar_contrasena: '',
    rol: '',
    codigo: '',
    idFacultad: '',
    codigoPrograma: '' as string | null,
    codigoUnidad: '' as string | null,
    notLeidas: ''
  };

  mensaje: string = '';
  esError = false;

  programas: Array<{ codigo: string; nombre: string } > = [];
  unidades: Array<{ codigo: string; nombre: string } > = [];
  facultades: Array<{ id: string; nombre: string } > = [];

  constructor(
    private apiService: Api,
    private programasService: ProgramasService,
    private unidadService: UnidadService,
    private facultadService: FacultadService
  ) {}

  ngOnInit() { 
    this.cargarListas(); 
  }
  
  private cargarListas() {
    this.programasService.listar().subscribe({ 
      next: (data) => { 
        this.programas = (data || []).map((p: any) => ({ 
          codigo: String(p.codigo || ''), 
          nombre: p.nombre || '' 
        })); 
        console.log('Programas cargados:', this.programas);
      },
      error: (err) => {
        console.error('Error al cargar programas:', err);
      }
    });
    this.unidadService.listar().subscribe({ 
      next: (data) => { 
        this.unidades = (data || []).map((u: any) => ({ 
          codigo: String(u.codigo || ''), 
          nombre: u.nombre || '' 
        })); 
      } 
    });
    this.facultadService.listar().subscribe({ 
      next: (data) => { 
        this.facultades = (data || []).map((f: any) => ({ 
          id: String(f.id || ''), 
          nombre: f.nombre || '' 
        })); 
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

  onRolChange(nuevoRol: string) {
    this.usuario.rol = nuevoRol;
    this.usuario.codigo = '';
    this.usuario.codigoPrograma = '';
    this.usuario.codigoUnidad = '';
    this.usuario.idFacultad = '';
  }

  getSelectValue(event: Event): string {
    return (event.target as HTMLSelectElement).value;
  }

  registrar() {
    const pwd = this.usuario.contrasena;
    const strong = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[^\w\s]).{8,64}$/.test(pwd) && !/\s/.test(pwd);
    if (!this.usuario.identificacion || !this.usuario.nombre?.trim() || !this.usuario.apellido?.trim() ||
        !this.usuario.correo?.trim() || !this.usuario.rol?.trim()) {
      this.mensaje = 'Hay campos obligatorios vacíos, llene todos antes de crear el usuario'; this.esError = true;
      return;
    }
    if (!strong) {
      this.mensaje = 'La contraseña debe tener 8-64 caracteres, incluir mayúscula, minúscula, número y caracter especial, y no tener espacios';
      this.esError = true;
      return;
    }

    if (!this.usuario.correo.endsWith('@uao.edu.co')) {
      this.mensaje = 'Debes usar tu correo institucional @uao.edu.co'; this.esError = true;
      return;
    }
    if (this.usuario.contrasena !== this.usuario.confirmar_contrasena) {
      this.mensaje = 'Las contraseñas no coinciden.'; this.esError = true;
      return;
    }
    // Reglas según rol
    const rol = this.mapRol(this.usuario.rol);
    if (rol === 'estudiante' && (!this.usuario.codigo || !this.usuario.codigoPrograma)) {
      this.mensaje = 'Para estudiante, código y programa son obligatorios'; this.esError = true;
      return;
    }
    if (rol === 'docente' && !this.usuario.codigoUnidad) {
      this.mensaje = 'Para docente, la unidad académica es obligatorio'; this.esError = true;
      return;
    }
    if (rol === 'secretaria_academica' && !this.usuario.idFacultad) {
      this.mensaje = 'Para secretaría académica, la facultad es obligatorio'; this.esError = true;
      return;
    }

    const campos = [
      this.usuario.nombre, this.usuario.apellido];

    for (const i of campos){
      if (this.contieneInyeccion(i)) {
        this.mensaje = 'No se permiten scripts o comandos en los campos de texto.';
        this.esError = true;
        return;
      }
    }

    const { confirmar_contrasena, ...formValues } = this.usuario;

    const toNum = (v: any) => (v === null || v === undefined || v === '' ? undefined : Number(v));
    const payload: UsuarioRegistroDto = {
    identificacion: Number(formValues.identificacion),
    nombre: formValues.nombre,
    apellido: formValues.apellido,
    correoInstitucional: formValues.correo,
    contrasena: formValues.contrasena,
    rol: this.mapRol(formValues.rol),
    codigo: formValues.codigo ? Number(formValues.codigo) : undefined,
    codigoPrograma: formValues.codigoPrograma || undefined, 
    codigoUnidad: formValues.codigoUnidad || undefined,    
    idFacultad: formValues.idFacultad || undefined,       
  };
 
    this.apiService.registrarUsuario(payload).subscribe({
      next: () => {
        this.mensaje = 'Usuario registrado exitosamente'; this.esError = false;
      },
      error: (err) => {
        const backendMsg = err?.error?.mensaje || err?.error?.message || 'Error al registrar usuario';
        this.mensaje = backendMsg; this.esError = true;
      }
    });
  }

  private mapRol(rolUi: string): string {
    if (rolUi === 'estudiante') return 'estudiante';
    if (rolUi === 'docente') return 'docente';
    if (rolUi === 'secretaria') return 'secretaria_academica';
    return rolUi;
  }
}