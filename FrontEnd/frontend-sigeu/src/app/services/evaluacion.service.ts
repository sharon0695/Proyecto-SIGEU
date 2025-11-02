import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_PATHS, buildApiUrl } from '../config/config';

@Injectable({ providedIn: 'root' })
export class EvaluacionService {
  private baseUrl = buildApiUrl(API_PATHS.evaluacion);

  constructor(private http: HttpClient) {}

  listarPendientes(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/pendientes`);
  }

  aprobar(codigo: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/aprobar/${codigo}`, {});
    }

  rechazar(codigo: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/rechazar/${codigo}`, {});
  }

  obtenerDetalle(codigo: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/detalle/${codigo}`);
  }
}


