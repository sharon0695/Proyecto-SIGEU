import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface Organizacion { nit: string; nombre: string; }

@Injectable({ providedIn: 'root' })
export class OrganizacionesService {
  constructor(private http: HttpClient) {}

  listar() { return this.http.get<Organizacion[]>('/api/OrganizacionExterna/listar'); }
}
