import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_PATHS, buildApiUrl } from '../config/config';

export interface Notificacion {
  id: number;
  remitente: number;
  destinatario: number;
  fecha: string;
  hora: string;
  detalles: string;
  leida?: boolean;
  fechaCompleta?: string;
  notLeidas?: number;
}

@Injectable({ providedIn: 'root' })
export class NotificacionService {
  private baseUrl = buildApiUrl(API_PATHS.notificacion);
  constructor(private http: HttpClient) {}

  obtenerNotificaciones(idUsuario: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/${idUsuario}`);
  }

  crear(body: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/ruta1`, body);
  }
  
  marcarComoLeidas(idUsuario: number) {
    return this.http.put(`${this.baseUrl}/leer/${idUsuario}`, {});
  }

}