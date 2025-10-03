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

import com.gestion.eventos.Model.EspacioModel;
import com.gestion.eventos.Service.IEspacioService;

@RestController
@RequestMapping ("/rutaEsp")
public class EspacioController {
    @Autowired IEspacioService espacioService;
    @PostMapping ("/ruta1")
    public ResponseEntity<EspacioModel> crearEspacio(@RequestBody EspacioModel espacio){
        return new ResponseEntity<>(espacioService.guardarEspacio(espacio),HttpStatus.CREATED);
    }
    @GetMapping ("/ruta2")
    public ResponseEntity<List<EspacioModel>> listarEspacios(){
        return new ResponseEntity<>(espacioService.listarEspacios(), HttpStatus.OK);
    }
}   

