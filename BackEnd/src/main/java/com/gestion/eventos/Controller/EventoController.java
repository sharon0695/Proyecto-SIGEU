package com.gestion.eventos.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gestion.eventos.DTO.EventoCompletoResponse;
import com.gestion.eventos.DTO.EventoEdicionCompleto;
import com.gestion.eventos.DTO.EventoRegistroCompleto;
import com.gestion.eventos.DTO.EventoRegistroResponse;
import com.gestion.eventos.Model.EventoModel;
import com.gestion.eventos.Service.IEventoService;


@RestController
@RequestMapping ("/eventos")
public class EventoController {
    @Autowired IEventoService eventoService;

    @PostMapping(value = "/registrar", consumes = "multipart/form-data")
    public ResponseEntity<?> registrarEvento(@ModelAttribute EventoRegistroCompleto request) {
            EventoModel eventoRegistrado = eventoService.registrarEventoCompleto(request);
            EventoRegistroResponse response = new EventoRegistroResponse(
                "Registro de evento exitoso. El evento se encuentra en estado borrador",
                eventoRegistrado.getCodigo()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping ("/listar")
    public ResponseEntity<List<EventoModel>> listarEventos(){
        return new ResponseEntity<>(eventoService.listarEventos(), HttpStatus.OK);
    }
    @PutMapping(value = "/editar", consumes = "multipart/form-data")
    public ResponseEntity<?> editarEvento(@ModelAttribute EventoEdicionCompleto request) {
            EventoModel eventoEditado = eventoService.editarEventoCompleto(request);
            EventoRegistroResponse response = new EventoRegistroResponse(
                "Edición de evento exitosa",
                eventoEditado.getCodigo()
            );
            return ResponseEntity.ok(response);
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<EventoCompletoResponse> obtenerEventoCompleto(@PathVariable Integer codigo) {
        EventoCompletoResponse eventoCompleto = eventoService.obtenerEventoCompleto(codigo);
        return ResponseEntity.ok(eventoCompleto);
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<?> eliminarEvento(@PathVariable Integer codigo) {
        eventoService.eliminarEvento(codigo);
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Evento eliminado correctamente");
        return ResponseEntity.ok(response);
    }

    //Filtrar por nombre 
    @GetMapping("/filtrar/nombre/{nombre}")
    public ResponseEntity<List<EventoModel>> filtrarPorNombre(@PathVariable String nombre){
        List<EventoModel> eventos = eventoService.filtrarPorNombre(nombre);
        return new ResponseEntity<>(eventos, HttpStatus.OK);
    }

    //Filtrar por estado
    @GetMapping("/filtrar/estado/{estado}")
    public ResponseEntity<List<EventoModel>> filtrarPorEstado(@PathVariable EventoModel.estado estado){
        List<EventoModel> eventos = eventoService.filtrarPorEstado(estado);
        return new ResponseEntity<>(eventos, HttpStatus.OK);
    }

    //Filtrar por fecha
    @GetMapping("/filtrar/fecha/{fecha}")
    public ResponseEntity<List<EventoModel>> filtrarPorFecha(
        @PathVariable 
        @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
        java.util.Date fecha
        ){
        List<EventoModel> eventos = eventoService.filtrarPorFecha(fecha);
        return new ResponseEntity<>(eventos, HttpStatus.OK);
    }

    //Coso del envio
    @PutMapping("/enviar/{codigo}")
    public ResponseEntity<?> enviarEvento(@PathVariable Integer codigo,
        @org.springframework.web.bind.annotation.RequestParam String usuario) {
        try {
            EventoModel evento = eventoService.enviarEventoAValidacion(codigo, usuario);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Evento enviado correctamente a Secretaría Académica");
            response.put("nuevoEstado", evento.getEstado().toString());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
    }
}

}   

