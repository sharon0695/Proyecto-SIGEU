package com.gestion.eventos.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gestion.eventos.DTO.LoginRequest;
import com.gestion.eventos.DTO.LoginResponse;
import com.gestion.eventos.Model.UsuarioModel;
import com.gestion.eventos.Security.JwtUtil;
import com.gestion.eventos.Security.TokenBlacklistService;
import com.gestion.eventos.Service.IUsuarioService;

@RestController
@RequestMapping ("/usuarios")
public class UsuarioController {
    @Autowired IUsuarioService usuarioService;
    @Autowired JwtUtil jwtUtil;
    @Autowired TokenBlacklistService tokenBlacklistService;
    
    @PostMapping ("/registrar")
    public ResponseEntity<UsuarioModel> crearUsuario(@RequestBody UsuarioModel usuario){
        return new ResponseEntity<>(usuarioService.guardarUsuario(usuario),HttpStatus.CREATED);
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = usuarioService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        usuarioService.logout(authHeader);
        return ResponseEntity.noContent().build(); // 204 sin contenido
    }
    
    @GetMapping ("/listar")
    public ResponseEntity<List<UsuarioModel>> listarUsuarios(){
        return new ResponseEntity<>(usuarioService.listarUsuario(), HttpStatus.OK);
    }

    @PostMapping("/recuperar")
    public ResponseEntity<String> recuperarContrasena(@RequestParam String correo) {
        usuarioService.enviarCredencialesPorCorreo(correo);
        return ResponseEntity.ok("Se ha enviado la contraseña al correo registrado");
    }

    @PutMapping("/editarPerfil")
    public ResponseEntity<UsuarioModel> editarPerfil(
        @RequestParam Integer identificacion,
        @RequestParam(required = false) String contrasena,
        @RequestParam(required = false) String celular) {
    
    UsuarioModel actualizado = usuarioService.actualizarPerfil(identificacion, contrasena, celular);
    return ResponseEntity.ok(actualizado);
    }
}   
