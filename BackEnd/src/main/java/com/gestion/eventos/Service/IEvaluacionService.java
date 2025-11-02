package com.gestion.eventos.Service;

import com.gestion.eventos.Model.EvaluacionModel;
import com.gestion.eventos.Model.EventoModel;
import com.gestion.eventos.Model.EventoModel.estado;

import java.util.List;

public interface IEvaluacionService {
    EvaluacionModel guardarEvaluacion(EvaluacionModel evaluacion);
    List<EvaluacionModel> listarEvaluaciones();
    List<EventoModel> listarPorEstado(estado estado);
    EventoModel aprobarEvento(Integer codigoEvento);
    EventoModel rechazarEvento(Integer codigoEvento);
}
