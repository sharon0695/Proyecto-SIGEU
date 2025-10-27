package com.gestion.eventos.Repository;

import com.gestion.eventos.Model.EventoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Date;

public interface IEventoRepository extends JpaRepository<EventoModel, Integer>{    
    //consultas

    //Filtrar por nombre ignorando mayus o minusculas
    List<EventoModel> findByNombreContainingIgnoreCase(String nombre);

    //Filtrar por estado (aprobado, rechazado, borrador, enviado, publicado)
    List<EventoModel> findByEstado(EventoModel.estado estado);

    //Filtrar por fecha
    List<EventoModel> findByFecha(Date fecha);

    //Filtrar por rango de fechas
    List<EventoModel> findByFechaBetween(Date fechaInicio, Date fechaFin);
}
