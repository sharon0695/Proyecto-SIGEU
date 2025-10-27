package com.gestion.eventos.Service;

import com.gestion.eventos.DTO.EventoCompletoResponse;
import com.gestion.eventos.DTO.EventoEdicionCompleto;
import com.gestion.eventos.DTO.EventoRegistroCompleto;
import com.gestion.eventos.Model.EventoModel;
import java.util.List;
import java.util.Optional;
import java.util.Date;


public interface IEventoService {
    List<EventoModel> listarEventos();
    List<EventoModel> filtrarPorNombre(String nombre);
    List<EventoModel> filtrarPorEstado(EventoModel.estado estado);
    List<EventoModel> filtrarPorFecha(Date fecha)
    EventoModel registrarEventoCompleto(EventoRegistroCompleto request);
    Optional<EventoModel> buscarPorCodigo(Integer codigo);
    void eliminarEvento(Integer codigo);
    EventoModel editarEventoCompleto(EventoEdicionCompleto request);
    EventoModel enviarEventoAValidacion(Integer codigo, String usuario)
    EventoCompletoResponse obtenerEventoCompleto(Integer codigo);
    
    
}