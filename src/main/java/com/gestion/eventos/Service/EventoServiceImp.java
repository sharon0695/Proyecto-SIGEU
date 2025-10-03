package com.gestion.eventos.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gestion.eventos.Model.EventoModel;
import com.gestion.eventos.Repository.IEventoRepository;
@Service
public class EventoServiceImp implements IEventoService{
    @Autowired IEventoRepository eventoRepository;

    @Override
    public EventoModel guardarEvento(EventoModel evento) {
        return eventoRepository.save(evento);    
    }

    @Override
    public List<EventoModel> listarEventos() {
        return eventoRepository.findAll();    
    }

}
