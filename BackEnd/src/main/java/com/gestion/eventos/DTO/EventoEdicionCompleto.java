package com.gestion.eventos.DTO;

import java.util.List;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class EventoEdicionCompleto {
    private Integer codigo;
    private String nombre;
    private String descripcion;
    private String tipo;
    private java.sql.Date fecha;
    private java.sql.Time hora_inicio;
    private java.sql.Time hora_fin;
    private Integer id_usuario_registra;
    private List<ColaboracionEdicionDTO> colaboraciones;
    private List<ResponsableDTO> responsables;
    private List<ReservacionDTO> reservaciones;

    @Data
    public static class ColaboracionEdicionDTO {
        private String nit; 
        private MultipartFile certificado_participacion; 
        private String certificado_existente; 
        private String representante_alterno;
    }

    @Data
    public static class ResponsableDTO {
        private Integer id_usuario;
        private MultipartFile documentoAval; 
        private String documento_existente; 
        private String tipoAval;
    }

    @Data
    public static class ReservacionDTO {
        private String codigo_espacio;
        private java.sql.Time hora_inicio;
        private java.sql.Time hora_fin;
    }
}