package com.gestion.eventos.Service;

import com.gestion.eventos.DTO.EventoRegistroCompleto;
import com.gestion.eventos.Model.EventoModel;
import java.util.List;

public interface IEventoService {
    List<EventoModel> listarEventos();
    EventoModel registrarEventoCompleto(EventoRegistroCompleto request);

}