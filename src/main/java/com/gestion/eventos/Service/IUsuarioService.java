package com.gestion.eventos.Service;

import java.util.List;

import com.gestion.eventos.Model.UsuarioModel;

public interface IUsuarioService {
    UsuarioModel guardarUsuario(UsuarioModel usuarios);
    UsuarioModel autenticarUsuario(String correoInstitucional, String contrasena);
    List<UsuarioModel> listarUsuario();
    void enviarCredencialesPorCorreo(String correo);
    UsuarioModel actualizarPerfil(Integer identificacion, String nuevaContrasena, String nuevoCelular);

}


    