package com.gestion.eventos.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gestion.eventos.Model.NotificacionModel;
import com.gestion.eventos.Repository.INotificacionRepository;
import com.gestion.eventos.Service.INotificacionService;

@RestController
@RequestMapping ("/notificaciones")
public class NotificacionController {
    @Autowired INotificacionService notificacionService;
    @Autowired INotificacionRepository notificacionRepository;
    @PostMapping ("/ruta1")
    public ResponseEntity<NotificacionModel> crearNotificacion(@RequestBody NotificacionModel notificacion){
        return new ResponseEntity<>(notificacionService.guardarNotificacion(notificacion),HttpStatus.CREATED);
    }
    @GetMapping("/notificaciones/{idUsuario}")
    public ResponseEntity<List<NotificacionModel>> obtenerNotificacionesPorUsuario(@PathVariable Integer idUsuario) {
        List<NotificacionModel> notificaciones = notificacionRepository.findByDestinatario(idUsuario);
        return ResponseEntity.ok(notificaciones);
    }

}   
