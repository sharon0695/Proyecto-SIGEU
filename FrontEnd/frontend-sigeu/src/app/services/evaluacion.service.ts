import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_PATHS, buildApiUrl } from '../config/config';

@Injectable({ providedIn: 'root' })
export class EvaluacionService {
  private baseUrl = buildApiUrl(API_PATHS.evaluacion);

  constructor(private http: HttpClient) {}

  listarPendientes(idSecre: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/pendientes/${idSecre}`);
  }

  obtenerDetalle(codigo: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/detalle/${codigo}`);
  }

  aprobar(idEvento: number, formData: FormData): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/aprobar/${idEvento}`, formData);
  }

  rechazar(idEvento:number, formData: FormData): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/rechazar/${idEvento}`, formData);
  }

  getFileViewUrl(tipo: string, filePath: string): string {
    const fileName = filePath.split('/').pop();
    return `http://localhost:8080/archivos/ver?tipo=${tipo}&archivo=${fileName}`;
  }

  getFileDownloadUrl(tipo: string, filePath: string): string {
    const fileName = filePath.split('/').pop();
    return `http://localhost:8080/archivos/descargar?tipo=${tipo}&archivo=${fileName}`;
  }
}


