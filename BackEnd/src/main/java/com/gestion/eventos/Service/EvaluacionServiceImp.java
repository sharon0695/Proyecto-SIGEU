package com.gestion.eventos.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.gestion.eventos.Model.EvaluacionModel;
import com.gestion.eventos.Model.EventoModel;
import com.gestion.eventos.Model.NotificacionModel;
import com.gestion.eventos.Repository.IEvaluacionRepository;
import com.gestion.eventos.Repository.IEventoRepository;
import com.gestion.eventos.Repository.INotificacionRepository;

@Service
public class EvaluacionServiceImp implements IEvaluacionService{
    @Autowired IEvaluacionRepository evaluacionRepository;
    @Autowired IEventoRepository eventoRepository;
    @Autowired FileStorageService fileStorageService;
    @Autowired INotificacionRepository notificacionRepository;

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
    public void aprobarEvento(Integer idEvento, String decision, MultipartFile actaComite, Integer idSecreAcad) {
        EventoModel evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        if (actaComite == null || actaComite.isEmpty()) {
            throw new IllegalArgumentException("Debe adjuntar el acta del comité en formato PDF.");
        }

        // Guardar archivo PDF (usa el método sobrecargado)
        String rutaActa = fileStorageService.storeFile(actaComite);

        // Crear registro de evaluación
        EvaluacionModel evaluacion = new EvaluacionModel();
        evaluacion.setCodigoEvento(evento);
        evaluacion.setDecision(decision);
        evaluacion.setActa_comite(rutaActa);
        evaluacionRepository.save(evaluacion);

        // Actualizar estado del evento
        evento.setEstado(EventoModel.estado.aprobado);
        eventoRepository.save(evento);

        // Notificar al organizador
        NotificacionModel notificacion = new NotificacionModel();
        notificacion.setRemitente(idSecreAcad);
        notificacion.setDetalles("Su evento \"" + evento.getNombre() + "\" fue aprobado.");
        notificacion.setDestinatario(evento.getIdUsuarioRegistra());
        notificacion.setFecha(new java.sql.Date(System.currentTimeMillis()));
        notificacion.setHora(new java.sql.Time(System.currentTimeMillis()));
        notificacionRepository.save(notificacion);
    }

    @Override
    public void rechazarEvento(Integer idEvento, String decision, String observaciones, Integer idSecreAcad) {
        EventoModel evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        if (observaciones == null || observaciones.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar una justificación para el rechazo.");
        }

        // Crear registro de evaluación
        EvaluacionModel evaluacion = new EvaluacionModel();
        evaluacion.setCodigoEvento(evento);
        evaluacion.setDecision(decision);
        evaluacion.setObservaciones(observaciones);
        evaluacionRepository.save(evaluacion);

        // Actualizar estado del evento
        evento.setEstado(EventoModel.estado.rechazado);
        eventoRepository.save(evento);

        // Notificar al organizador
        NotificacionModel notificacion = new NotificacionModel();
        notificacion.setRemitente(idSecreAcad);
        notificacion.setDetalles("Su evento \"" + evento.getNombre() + "\" fue rechazado. Motivo: " + observaciones);
        notificacion.setDestinatario(evento.getIdUsuarioRegistra());
        notificacion.setFecha(new java.sql.Date(System.currentTimeMillis()));
        notificacion.setHora(new java.sql.Time(System.currentTimeMillis()));
        notificacionRepository.save(notificacion);
    }
}