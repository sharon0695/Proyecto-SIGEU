package com.gestion.eventos.Service;

import java.util.List;

import com.gestion.eventos.DTO.LoginRequest;
import com.gestion.eventos.DTO.LoginResponse;
import com.gestion.eventos.Model.UsuarioModel;

public interface IUsuarioService {
    UsuarioModel guardarUsuario(UsuarioModel usuarios);
    LoginResponse login(LoginRequest request);
    List<UsuarioModel> listarUsuario();
    void enviarCredencialesPorCorreo(String correo);
    UsuarioModel actualizarPerfil(Integer identificacion, String nuevaContrasena, String nuevoCelular);
    void logout(String authHeader);
}


    