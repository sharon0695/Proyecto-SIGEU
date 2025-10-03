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

import com.gestion.eventos.Model.ProgramaModel;
import com.gestion.eventos.Service.IProgramaService;

@RestController
@RequestMapping ("/rutaPro")
public class ProgramaController {
    @Autowired IProgramaService programaService;
    @PostMapping ("/ruta1")
    public ResponseEntity<ProgramaModel> crearPrograma(@RequestBody ProgramaModel programa){
        return new ResponseEntity<>(programaService.guardarPrograma(programa),HttpStatus.CREATED);
    }
    @GetMapping ("/ruta2")
    public ResponseEntity<List<ProgramaModel>> listarProgramas(){
        return new ResponseEntity<>(programaService.listarProgramas(), HttpStatus.OK);
    }
}   


