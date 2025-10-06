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
    public EventoModel registrarEvento(EventoModel evento) {
        try {
            // Validar campos obligatorios según el diagrama de secuencia
            validarCamposEvento(evento);
            
            // Establecer estado inicial como borrador
            //evento.setEstado("borrador");

            return eventoRepository.save(evento);

        } catch (Exception e) {
            throw new RuntimeException("Error al registrar el evento: " + e.getMessage(), e);
        }
    }

    private void validarCamposEvento(EventoModel evento) {
        if (evento.getNombre() == null || evento.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del evento es obligatorio");
        }
        if (evento.getDescripcion() == null || evento.getDescripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción del evento es obligatoria");
        }
        if (evento.getTipo() == null || evento.getTipo().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo del evento es obligatorio");
        }
        if (evento.getFecha() == null) {
            throw new IllegalArgumentException("La fecha del evento es obligatoria");
        }
        if (evento.getHora_fin() == null) {
            throw new IllegalArgumentException("La hora de fin del evento es obligatoria");
        }
        if (evento.getCodigo_lugar() == null || evento.getCodigo_lugar().trim().isEmpty()) {
            throw new IllegalArgumentException("El código del lugar es obligatorio");
        }
    }

    @Override
    public List<EventoModel> listarEventos() {
        return eventoRepository.findAll();    
    }

}
