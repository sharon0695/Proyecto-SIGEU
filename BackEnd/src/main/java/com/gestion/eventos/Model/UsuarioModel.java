package com.gestion.eventos.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table (name ="Usuario")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioModel {
    @Id
    private Integer identificacion;
    private Integer codigo;
    private String nombre;
    private String apellido;
    @Column(name = "correo_institucional")
    private String correoInstitucional;
    private String contrasena;
    public enum rol{estudiante, docente, secretaria_academica, administrador}
    @Enumerated(EnumType.STRING)
    private rol rol;
    private String celular;
    @ManyToOne
    @JoinColumn (name="id_facultad")
    private FacultadModel idFacultad;
    @ManyToOne
    @JoinColumn (name="codigo_programa")
    private ProgramaModel codigo_programa;
    @ManyToOne
    @JoinColumn (name="codigo_unidad")
    private UnidadAcademicaModel codigo_unidad;
    private Integer notLeidas;
    
    // Campos para bloqueo por intentos fallidos
    @Column(name = "intentos_fallidos")
    private Integer intentosFallidos = 0;
    
    @Column(name = "bloqueado_hasta")
    private java.time.LocalDateTime bloqueadoHasta;
}
