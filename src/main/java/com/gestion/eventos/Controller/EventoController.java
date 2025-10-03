package com.gestion.eventos.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gestion.eventos.Model.EventoModel;
import com.gestion.eventos.Service.IEventoService;


@RestController
@RequestMapping ("/rutaEve")
public class EventoController {
    @Autowired IEventoService eventoService;
    @PostMapping ("/ruta1")
    public ResponseEntity<EventoModel> crearEvento(@RequestBody EventoModel evento){
        return new ResponseEntity<>(eventoService.guardarEvento(evento),HttpStatus.CREATED);
    }
    @GetMapping ("/ruta2")
    public ResponseEntity<List<EventoModel>> listarEventos(){
        return new ResponseEntity<>(eventoService.listarEventos(), HttpStatus.OK);
    }
}   

