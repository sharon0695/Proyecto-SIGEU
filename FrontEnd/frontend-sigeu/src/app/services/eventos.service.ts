import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_PATHS, buildApiUrl } from '../config/config';

export interface EventoRegistroCompleto {
  nombre: string;
  descripcion?: string;
  tipo: string;
  fecha: string;
  hora_inicio: string;
  hora_fin: string;
  id_usuario_registra: number;
  organizaciones: OrganizacionDTO[];
  responsables: ResponsableDTO[];
  reservaciones: ReservacionDTO[];
}

export interface EventoEdicionCompleto {
  codigo: number;
  nombre: string;
  descripcion?: string;
  tipo: string;
  fecha: string;
  hora_inicio: string;
  hora_fin: string;
  id_usuario_registra: number;
  organizaciones: OrganizacionDTO[];
  responsables: ResponsableDTO[];
  reservaciones: ReservacionDTO[];
}

export interface OrganizacionDTO {
  nit: string;
  nombre?: string;
  representante_legal?: string;
  ubicacion?: string;
  telefono?: string;
  sector_economico?: string;
  actividad_principal?: string;
  certificado_participacion?: string;
  representante_alterno?: string;
}

export interface ResponsableDTO {
  id_usuario: number;
  documentoAval?: string;
  tipoAval?: string;
}

export interface ReservacionDTO {
  codigo_espacio: string;
  hora_inicio: string;
  hora_fin: string;
}

@Injectable({ providedIn: 'root' })
export class EventosService {
  url: any;
  obtenerDetallesEvaluacion(codigo: number): Observable<any> {
    return this.http.get(`${this.url}/detalles-evaluacion/${codigo}`);
  }
  
  private baseUrl = buildApiUrl(API_PATHS.eventos);
  private baseArcUrl = 'http://localhost:8080/archivos';
  constructor(private http: HttpClient) {}

  listar(idUsuario: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/listar`, { params: { idUsuario }});
  }

  registrar(formData: FormData): Observable<any> {
    return this.http.post(`${this.baseUrl}/registrar`, formData);
  }

  editar(formData: FormData): Observable<any> {
    return this.http.put(`${this.baseUrl}/editar`, formData);
  }

  obtenerDetalles(codigo: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/${codigo}`);
  }

  obtenerParaEdicion(codigo: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/edicion/${codigo}`);
  }

  getFileViewUrl(folder: string, rutaRelativa: string): string {
    const partes = rutaRelativa.split('/');
    const evento = partes[1]; 
    const filename = partes[2];
    return `${this.baseArcUrl}/view/${folder}/${evento}/${filename}`;
  }

  getFileDownloadUrl(folder: string, filePath: string) {
    const parts = filePath.split('/');
    const evento = parts[1];
    const filename = parts[2];
    return `${this.baseArcUrl}/download/${folder}/${evento}/${filename}`;
  }

  eliminarEvento(codigo: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${codigo}`);
  }

  enviarEvento(codigo: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/enviar/${codigo}`, {}); 
  }

}
