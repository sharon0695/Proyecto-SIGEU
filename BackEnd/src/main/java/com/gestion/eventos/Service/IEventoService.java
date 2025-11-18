package com.gestion.eventos.Service;

import com.gestion.eventos.DTO.EventoCompletoResponse;
import com.gestion.eventos.DTO.EventoEdicionCompleto;
import com.gestion.eventos.DTO.EventoRegistroCompleto;
import com.gestion.eventos.Model.EventoModel;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public interface IEventoService {
    List<EventoModel> listarEventos();
    EventoModel registrarEventoCompleto(EventoRegistroCompleto request);
    Optional<EventoModel> buscarPorCodigo(Integer codigo);
    void eliminarEvento(Integer codigo);
    EventoModel editarEventoCompleto(EventoEdicionCompleto request);
    EventoCompletoResponse obtenerEventoCompleto(Integer codigo);
    List<EventoModel> listarPorUsuario(Integer idUsuario);
    EventoModel enviarEventoAValidacion(Integer codigoEvento);
    Map<String, Object> obtenerDetallesEvaluacion(Integer codigoEvento);
}