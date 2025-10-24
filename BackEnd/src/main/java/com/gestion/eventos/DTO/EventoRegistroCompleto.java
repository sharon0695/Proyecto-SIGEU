package com.gestion.eventos.DTO;

import java.sql.Date;
import java.sql.Time;
import java.util.List;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class EventoRegistroCompleto {
    private String nombre;
    private String descripcion;
    private String tipo;
    private Date fecha;
    private Time hora_inicio;
    private Time hora_fin;
    private Integer id_usuario_registra; 
    private List<ColaboracionDTO> colaboraciones; 
    private List<ResponsableDTO> responsables;    
    private List<ReservacionDTO> reservaciones;
    
    @Data
    public static class ColaboracionDTO {
        private String nit; 
        private MultipartFile certificado_participacion;
        private String representante_alterno; 
    } 
    
    @Data
    public static class ResponsableDTO {
        private Integer id_usuario;
        private MultipartFile documentoAval;
        private String tipoAval; 
    }
    
    @Data
    public static class ReservacionDTO {
        private String codigo_espacio;
        private Time hora_inicio;
        private Time hora_fin;
    }
}