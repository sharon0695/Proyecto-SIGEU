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
@Table (name = "Evaluacion")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvaluacionModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn (name="codigo_evento")
    private EventoModel codigoEvento;
    @ManyToOne
    @JoinColumn (name="id_secreAcad")
    private UsuarioModel idSecreAcad;
    private String observaciones;
    private String acta_comite;
    private String decision;
}
