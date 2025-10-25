package com.gestion.eventos.Controller;

import com.gestion.eventos.DTO.EventoCompletoResponse;
import com.gestion.eventos.DTO.EventoEdicionCompleto;
import com.gestion.eventos.DTO.EventoRegistroCompleto;
import com.gestion.eventos.DTO.EventoRegistroResponse;
import com.gestion.eventos.Model.EventoModel;
import com.gestion.eventos.Service.IEventoService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping ("/eventos")
public class EventoController {
    @Autowired IEventoService eventoService;

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarEvento(@RequestBody EventoRegistroCompleto request) {
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
    @PutMapping("/editar")
    public ResponseEntity<?> editarEvento(@RequestBody EventoEdicionCompleto request) {
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
}   

