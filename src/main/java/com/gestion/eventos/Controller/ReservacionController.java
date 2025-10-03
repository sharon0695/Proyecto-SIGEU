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

import com.gestion.eventos.Model.ReservacionModel;
import com.gestion.eventos.Service.IReservacionService;

@RestController
@RequestMapping ("/rutaRes")
public class ReservacionController {
    @Autowired IReservacionService reservacionService;
    @PostMapping ("/ruta1")
    public ResponseEntity<ReservacionModel> crearReservacion(@RequestBody ReservacionModel reservacion){
        return new ResponseEntity<>(reservacionService.guardarReservacion(reservacion),HttpStatus.CREATED);
    }
    @GetMapping ("/ruta2")
    public ResponseEntity<List<ReservacionModel>> listarReservaciones(){
        return new ResponseEntity<>(reservacionService.listarReservaciones(), HttpStatus.OK);
    }
}   
