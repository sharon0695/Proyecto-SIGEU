package com.gestion.eventos.DTO;

import java.sql.Time;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class EventoPublicoDTO {

    private Integer codigo;
    private String nombre;
    private String descripcion;
    private String tipo;

    private Date fecha;
    private Time hora_inicio;
    private Time hora_fin;

    private List<EspacioDTO> espacios;

    @Data
    public static class EspacioDTO {
        private String nombreEspacio;
        private Time horaInicio;
        private Time horaFin;
    }
}
