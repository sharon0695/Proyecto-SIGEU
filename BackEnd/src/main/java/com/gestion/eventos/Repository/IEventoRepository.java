package com.gestion.eventos.Repository;

import com.gestion.eventos.Model.EventoModel;
import com.gestion.eventos.Model.EventoModel.estado;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.time.LocalDate;
import java.util.Date;
import java.sql.Time;
import java.util.Optional;

public interface IEventoRepository extends JpaRepository<EventoModel, Integer>{    
    List<EventoModel> findByNombreContainingIgnoreCase(String nombre);
    List<EventoModel> findByEstado(estado estadoEnum);
    List<EventoModel> findByFecha(LocalDate fecha);
    List<EventoModel> findByFechaBetween(Date fechaInicio, Date fechaFin);
    List<EventoModel> findByIdUsuarioRegistra(Integer idUsuario);
    
    // Validación de duplicidad: buscar eventos con mismo nombre, fecha y hora de inicio
    @Query(value = "SELECT * FROM Evento WHERE nombre = :nombre AND fecha = :fecha AND hora_inicio = :horaInicio", nativeQuery = true)
    Optional<EventoModel> findByNombreAndFechaAndHoraInicio(
        @Param("nombre") String nombre, 
        @Param("fecha") Date fecha, 
        @Param("horaInicio") Time horaInicio
    );

}
