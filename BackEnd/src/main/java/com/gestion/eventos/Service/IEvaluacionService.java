package com.gestion.eventos.Service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.gestion.eventos.Model.EvaluacionModel;
import com.gestion.eventos.Model.EventoModel;
import com.gestion.eventos.Model.EventoModel.estado;

public interface IEvaluacionService {
    EvaluacionModel guardarEvaluacion(EvaluacionModel evaluacion);
    List<EvaluacionModel> listarEvaluaciones();
    void aprobarEvento(Integer idEvento, String decision, Integer idSecreAcad, MultipartFile actaComite);
    void rechazarEvento(Integer idEvento, String decision, Integer idSecreAcad, String observaciones);
    List<EventoModel> listarPorEstado(estado estado, Integer idUsuarioSecretaria);
}