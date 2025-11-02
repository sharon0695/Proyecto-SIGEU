package com.gestion.eventos.Service;

import com.gestion.eventos.Model.EvaluacionModel;
import com.gestion.eventos.Model.EventoModel;
import com.gestion.eventos.Repository.IEvaluacionRepository;
import com.gestion.eventos.Repository.IEventoRepository;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class EvaluacionServiceImp implements IEvaluacionService{
    @Autowired IEvaluacionRepository evaluacionRepository;
    @Autowired private IEventoRepository eventoRepository;
    
    @Override
    public EvaluacionModel guardarEvaluacion(EvaluacionModel evaluacion) {
        return evaluacionRepository.save(evaluacion);
    }

    @Override
    public List<EvaluacionModel> listarEvaluaciones() {
        return evaluacionRepository.findAll();    
    }

    @Override
    public List<EventoModel> listarPorEstado(EventoModel.estado estado) {
        return eventoRepository.findByEstado(estado);
    }

    @Override
    public EventoModel aprobarEvento(Integer codigoEvento) {
        EventoModel evento = eventoRepository.findById(codigoEvento)
            .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        if (evento.getEstado() != EventoModel.estado.enviado) {
            throw new RuntimeException("Solo se pueden aprobar eventos en estado 'enviado'");
        }
        evento.setEstado(EventoModel.estado.aprobado);
        return eventoRepository.save(evento);
    }

    @Override
    public EventoModel rechazarEvento(Integer codigoEvento) {
        EventoModel evento = eventoRepository.findById(codigoEvento)
            .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        if (evento.getEstado() != EventoModel.estado.enviado) {
            throw new RuntimeException("Solo se pueden rechazar eventos en estado 'enviado'");
        }
        evento.setEstado(EventoModel.estado.rechazado);
        return eventoRepository.save(evento);
    }

}
