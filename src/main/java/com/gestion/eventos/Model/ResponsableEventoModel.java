package com.gestion.eventos.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table (name ="Responsable_evento")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponsableEventoModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer consecutivo;
    @ManyToOne
    @JoinColumn (name = "id_usuario")
    private UsuarioModel id_usuario;
    @ManyToOne
    @JoinColumn (name = "codigo_evento")
    private EventoModel codigo_evento;
    private String documentoAval;
    public enum tipo_aval{director_programa, director_docencia}
    private tipo_aval tipoAval;
}
