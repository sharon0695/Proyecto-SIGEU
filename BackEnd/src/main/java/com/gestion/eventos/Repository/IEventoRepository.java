package com.gestion.eventos.Repository;

import com.gestion.eventos.Model.EventoModel;
import com.gestion.eventos.Model.EventoModel.estado;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDate;
import java.util.Date;

public interface IEventoRepository extends JpaRepository<EventoModel, Integer>{    
    List<EventoModel> findByNombreContainingIgnoreCase(String nombre);
    List<EventoModel> findByEstado(estado estadoEnum);
    List<EventoModel> findByFecha(LocalDate fecha);
    List<EventoModel> findByFechaBetween(Date fechaInicio, Date fechaFin);
    List<EventoModel> findByIdUsuarioRegistra(Integer idUsuario);

}
