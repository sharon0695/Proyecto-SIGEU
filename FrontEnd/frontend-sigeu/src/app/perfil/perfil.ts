import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Api } from '../services/usuarios.service'; 
import { PerfilService } from '../services/perfil.service';
import { Notificaciones } from '../notificaciones/notificaciones';

@Component({
  selector: 'app-perfil',
  imports: [CommonModule, FormsModule, RouterLink, Notificaciones],
  templateUrl: './perfil.html',
  styleUrl: './perfil.css'
})
export class Perfil {
  mensaje = '';
  usuario: any = null;
  celular: string ='';
  showEdit = false;
  edit = { celular: '', contrasenaActual: '', nuevaContrasena: '' };

  constructor(private auth: AuthService, private router: Router, private api: Api, private perfil: PerfilService) {}
  rol: string = '';
  ngOnInit() {
    this.cargarPerfil();
    this.rol = (this.auth.getUserRole() || '').toLowerCase();
    this.obtenerCelular();
  }

  private cargarPerfil() {
    const raw = localStorage.getItem('auth_user');
    this.usuario = raw ? JSON.parse(raw) : null;
    this.actualizarVistaPreviaImagen();
  }

  private actualizarVistaPreviaImagen() {
    const preview = document.getElementById('preview-avatar') as HTMLImageElement;
    if (preview && this.usuario?.fotoPerfil) {
      preview.src = `/api/usuarios/foto/${this.usuario.identificacion}`;
    } else if (preview) {
      preview.src = 'img/perfil.png';
    }
  }
  private obtenerCelular() {
    const idUsuario = this.auth.getUserId();
    if (!idUsuario) {
      console.error('No hay usuario autenticado.');
      return;
    }

    this.api.getUsuarios().subscribe({
      next: (usuarios) => {
        const usuarioActual = usuarios.find((u: any) => u.identificacion === idUsuario);
        if (usuarioActual) {
          this.celular = usuarioActual.celular || 'No registrado';
        } else {
          this.celular = 'No encontrado';
        }
      },
      error: (err) => {
        console.error('Error al obtener usuarios:', err);
        this.celular = 'Error al cargar';
      }
    });
  }

  openEdit() { 
    this.showEdit = true; 
    this.edit = { celular: this.usuario?.celular || '', contrasenaActual: '', nuevaContrasena: '' };
    this.actualizarVistaPreviaImagen();
  }
  closeEdit() { 
    this.showEdit = false; 
    this.mensaje = '';
    this.edit = { celular: '', contrasenaActual: '', nuevaContrasena: '' };
  }

  onLogout() {
    if (!confirm('¿Seguro que quieres cerrar sesión?')) return;
    this.auth.logoutRemote().subscribe({
      next: () => {
        this.auth.logout();
        this.mensaje = 'Sesión cerrada';
        this.router.navigateByUrl('/login');
      },
      error: () => {
        this.auth.logout();
        this.router.navigateByUrl('/login');
      }
    });
  }

  onSaveEdit() {
    //Verificar si hay intento de cambio de contraseña
    if (this.edit.nuevaContrasena || this.edit.contrasenaActual || this.edit.confirmarContrasena) {

        // Validar que la nueva contraseña y la confirmación no estén vacías
        if (!this.edit.nuevaContrasena.trim() || !this.edit.confirmarContrasena.trim()) {
            this.mensaje = 'Debes ingresar y confirmar la nueva contraseña.';
            setTimeout(() => this.mensaje = '', 3000);
            return;
        }

        // Validar Coincidencia
        if (this.edit.nuevaContrasena !== this.edit.confirmarContrasena) {
            this.mensaje = 'La nueva contraseña y la confirmación no coinciden.';
            setTimeout(() => this.mensaje = '', 3000);
            return; // Detiene la ejecución si no coinciden
        }

        // Validar que la contraseña anterior no esté vacía si se intenta cambiar
        if (!this.edit.contrasenaActual.trim()) {
            this.mensaje = 'Debes ingresar tu contraseña actual para realizar el cambio.';
            setTimeout(() => this.mensaje = '', 3000);
            return;
        }
    }
    
    // Si la validación de contraseñas es exitosa o si solo se edita el teléfono:
    
    if (!this.usuario?.identificacion) { 
        this.mensaje = 'No hay usuario cargado'; 
        return; 
    }

    const fileInput = document.getElementById('new-avatar') as HTMLInputElement | null;
    const foto = fileInput?.files?.[0];

    // Se utiliza this.edit.contrasenaActual para la contraseña anterior
    this.perfil.actualizarPerfil(this.usuario.identificacion, {
        contrasenaActual: this.edit.contrasenaActual || undefined,
        nuevaContrasena: this.edit.nuevaContrasena || undefined,
        celular: this.edit.celular || undefined,
        fotoFile: foto || undefined,
    }).subscribe({
        next: (usuarioActualizado) => {
            this.mensaje = 'Perfil actualizado correctamente';

            if (usuarioActualizado) {
                localStorage.setItem('auth_user', JSON.stringify(usuarioActualizado));
                this.usuario = usuarioActualizado;
            }

            this.closeEdit();
            this.cargarPerfil();
            
            // Reiniciar el objeto edit después de un cambio exitoso
            this.edit = { celular: '', contrasenaActual: '', nuevaContrasena: '', confirmarContrasena: '' }; // 🎯 Resetear también el nuevo campo

            setTimeout(() => this.mensaje = '', 3000);
        },
        error: (err) => {
            const mensajeError = err?.error?.message || err?.error?.mensaje || 'Error al actualizar perfil. Verifica tu contraseña anterior.';
            this.mensaje = mensajeError;
            setTimeout(() => this.mensaje = '', 3000);
        }
    });
  }

}
