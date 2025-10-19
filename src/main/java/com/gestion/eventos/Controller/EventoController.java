package com.gestion.eventos.Controller;

import com.gestion.eventos.DTO.EventoRegistroCompleto;
import com.gestion.eventos.DTO.EventoRegistroResponse;
import com.gestion.eventos.DTO.MensajeResponse;
import com.gestion.eventos.Model.EventoModel;
import com.gestion.eventos.Service.IEventoService;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


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
    
}   

