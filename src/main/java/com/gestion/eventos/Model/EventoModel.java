package com.gestion.eventos.Model;

import java.sql.Date;
import java.sql.Time;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table (name ="Evento")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventoModel {
    @Id
    private Integer codigo;
    private String nombre;
    private String descripcion;
    private String tipo;
    private Date fecha;
    private Time hora_inicio;
    private Time hora_fin;
    public enum estado{aprobado, rechazado, borrado, enviado, publicado}
    private estado estado;
    private String codigo_lugar;
    private String nit_organizacion;
}
