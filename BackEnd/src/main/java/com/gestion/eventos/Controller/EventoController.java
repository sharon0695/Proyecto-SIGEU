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
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<List<EventoModel>> listarEventosPorUsuario(@RequestParam Integer idUsuario) {
        List<EventoModel> eventos = eventoService.listarPorUsuario(idUsuario);
        return ResponseEntity.ok(eventos);
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

    @PutMapping("/enviar/{codigo}")
    public ResponseEntity<?> enviarEvento(@PathVariable Integer codigo) {
      
            EventoModel evento = eventoService.enviarEventoAValidacion(codigo);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Evento enviado correctamente a Secretaría Académica");
            response.put("nuevoEstado", evento.getEstado().toString());
            return ResponseEntity.ok(response);       
    }
    @GetMapping("/detalles-evaluacion/{codigo}")
    public ResponseEntity<Map<String, Object>> obtenerDetallesEvaluacion(@PathVariable Integer codigo) {
        Map<String, Object> detalles = eventoService.obtenerDetallesEvaluacion(codigo);
        return ResponseEntity.ok(detalles);
    }
}


