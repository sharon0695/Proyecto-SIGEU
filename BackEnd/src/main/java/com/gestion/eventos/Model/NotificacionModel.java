package com.gestion.eventos.Model;

import java.sql.Date;
import java.sql.Time;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table (name ="Notificacion")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer remitente;
    private Integer destinatario;
    private Time hora;
    private Date fecha;
    private String detalles;
    private Boolean leida;
}
