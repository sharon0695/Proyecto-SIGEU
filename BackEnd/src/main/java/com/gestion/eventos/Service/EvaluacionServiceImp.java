package com.gestion.eventos.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.gestion.eventos.Model.EvaluacionModel;
import com.gestion.eventos.Model.EventoModel;
import com.gestion.eventos.Model.UsuarioModel;
import com.gestion.eventos.Repository.IEvaluacionRepository;
import com.gestion.eventos.Repository.IEventoRepository;
import com.gestion.eventos.Repository.IUsuarioRepository;
@Service
public class EvaluacionServiceImp implements IEvaluacionService{
    @Autowired IEvaluacionRepository evaluacionRepository;
    @Autowired private IEventoRepository eventoRepository;
    @Autowired private IUsuarioRepository usuarioRepository;
    @Autowired private FileStorageService fileStorageService;
    
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
    public EventoModel aprobarEvento(Integer codigoEvento, Integer idSecretaria, String observaciones, MultipartFile actaComite) {
        EventoModel evento = eventoRepository.findById(codigoEvento)
            .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        
        if (evento.getEstado() != EventoModel.estado.enviado) {
            throw new RuntimeException("Solo se pueden aprobar eventos en estado 'enviado'");
        }

        // Validar que la secretaria existe
        UsuarioModel secretaria = usuarioRepository.findById(idSecretaria)
            .orElseThrow(() -> new RuntimeException("Secretaria académica no encontrada"));

        // Cambiar estado del evento
        evento.setEstado(EventoModel.estado.aprobado);
        eventoRepository.save(evento);

        // Crear evaluación
        EvaluacionModel evaluacion = new EvaluacionModel();
        evaluacion.setCodigo_evento(evento);
        evaluacion.setId_secreAcad(secretaria);
        evaluacion.setObservaciones(observaciones != null ? observaciones : "");

        // Guardar archivo si existe
        if (actaComite != null && !actaComite.isEmpty()) {
            if (!actaComite.getContentType().equals("application/pdf")) {
                throw new RuntimeException("El acta del comité debe ser un archivo PDF");
            }
            String actaPath = fileStorageService.storeFile(actaComite, "evaluaciones/evento_" + codigoEvento);
            evaluacion.setActa_comite(actaPath);
        }

        evaluacionRepository.save(evaluacion);

        return evento;
    }

    @Override
    public EventoModel rechazarEvento(Integer codigoEvento, Integer idSecretaria, String observaciones, MultipartFile actaComite) {
        EventoModel evento = eventoRepository.findById(codigoEvento)
            .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        
        if (evento.getEstado() != EventoModel.estado.enviado) {
            throw new RuntimeException("Solo se pueden rechazar eventos en estado 'enviado'");
        }

        // Validar que la secretaria existe
        UsuarioModel secretaria = usuarioRepository.findById(idSecretaria)
            .orElseThrow(() -> new RuntimeException("Secretaria académica no encontrada"));

        // Cambiar estado del evento
        evento.setEstado(EventoModel.estado.rechazado);
        eventoRepository.save(evento);

        // Crear evaluación
        EvaluacionModel evaluacion = new EvaluacionModel();
        evaluacion.setCodigo_evento(evento);
        evaluacion.setId_secreAcad(secretaria);
        evaluacion.setObservaciones(observaciones != null ? observaciones : "");

        // Guardar archivo si existe
        if (actaComite != null && !actaComite.isEmpty()) {
            if (!actaComite.getContentType().equals("application/pdf")) {
                throw new RuntimeException("El acta del comité debe ser un archivo PDF");
            }
            String actaPath = fileStorageService.storeFile(actaComite, "evaluaciones/evento_" + codigoEvento);
            evaluacion.setActa_comite(actaPath);
        }

        evaluacionRepository.save(evaluacion);

        return evento;
    }
}
