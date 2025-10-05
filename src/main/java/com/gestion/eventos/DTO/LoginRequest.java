package com.gestion.eventos.DTO;

import lombok.Data;

@Data
public class LoginRequest {
    private String correoInstitucional;
    private String contrasena;
}