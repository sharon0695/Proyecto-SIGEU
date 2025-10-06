package com.gestion.eventos.Service;

import java.util.List;

import com.gestion.eventos.Model.EventoModel;

public interface IEventoService {
    EventoModel guardarEvento(EventoModel evento);
    List<EventoModel> listarEventos();
    EventoModel registrarEvento(EventoModel evento);
}
