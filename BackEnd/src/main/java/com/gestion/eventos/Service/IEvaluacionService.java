package com.gestion.eventos.Service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.gestion.eventos.Model.EvaluacionModel;
import com.gestion.eventos.Model.EventoModel;
import com.gestion.eventos.Model.EventoModel.estado;

public interface IEvaluacionService {
    EvaluacionModel guardarEvaluacion(EvaluacionModel evaluacion);
    List<EvaluacionModel> listarEvaluaciones();
    List<EventoModel> listarPorEstado(estado estado);
    void aprobarEvento(Integer idEvento, String decision, MultipartFile actaComite, Integer idSecreAcad);
    void rechazarEvento(Integer idEvento, String decision, String observaciones, Integer idSecreAcad);
}