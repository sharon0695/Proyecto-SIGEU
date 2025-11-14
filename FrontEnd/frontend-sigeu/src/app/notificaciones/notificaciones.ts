import { Component} from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificacionService, Notificacion } from '../services/notificacion.service';
import { AuthService } from '../services/auth.service';
@Component({
  selector: 'app-notificaciones',
  imports: [CommonModule],
  templateUrl: './notificaciones.html',
  styleUrls: ['./notificaciones.css']
})
export class Notificaciones{

  notificacionesUsuario: Notificacion[] = [];
  usuarioId: number = 0;
  nuevas: number = 0;
  visible: boolean = false;
  loading: boolean = false;
  errorMsg: string = '';

  constructor(
    private notificacionService: NotificacionService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.usuarioId = this.authService.getUserId()??0;
    if (this.usuarioId !== null) {
      this.cargarNotificaciones();
    }
  }

  mostrar(): void {
    this.visible = true;

    // Marcar como leídas en la BD
    this.notificacionService.marcarComoLeidas(this.usuarioId).subscribe({
      next: () => {
        // También actualizamos en el front
        this.notificacionesUsuario = this.notificacionesUsuario.map(n => ({
          ...n,
          leida: true
        }));
        this.actualizarContador();
      }
    });
  }

  cerrar(): void {
    this.visible = false;
  }

  cargarNotificaciones(): void {
    this.loading = true;
    this.errorMsg = '';

    this.notificacionService.obtenerNotificaciones(this.usuarioId).subscribe({
      next: (data) => {
        this.notificacionesUsuario = data
          .map(n => ({
            ...n,
            fechaCompleta: new Date(`${n.fecha}T${n.hora}`)
          }))
          .sort((a, b) => b.fechaCompleta.getTime() - a.fechaCompleta.getTime());

        this.actualizarContador();
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMsg = 'No se pudieron cargar las notificaciones';
      }
    });
  }


  actualizarContador() {
    this.nuevas = this.notificacionesUsuario.filter(n => !n.leida).length;
  }
}
