package com.gestion.eventos.DTO;

import java.sql.Date;
import java.sql.Time;
import java.util.List;
import lombok.Data;

@Data
public class EventoCompletoResponse {
    private Integer codigo;
    private String nombre;
    private String descripcion;
    private String tipo;
    private Date fecha;
    private Time hora_inicio;
    private Time hora_fin;
    private String estado;
    private List<OrganizacionResponse> organizaciones;
    private List<ResponsableResponse> responsables;
    private List<ReservacionResponse> reservaciones;

    @Data
    public static class OrganizacionResponse {
        private String nit;
        private String nombre;
        private String representante_legal;
        private String ubicacion;
        private String telefono;
        private String sector_economico;
        private String actividad_principal;
        private String certificado_participacion;
        private String representante_alterno;
    }

    @Data
    public static class ResponsableResponse {
        private Integer id_usuario;
        private String nombreUsuario;
        private String documentoAval;
        private String tipoAval;
    }

    @Data
    public static class ReservacionResponse {
        private String codigo_espacio;
        private String nombreEspacio;
        private Time hora_inicio;
        private Time hora_fin;
    }
}