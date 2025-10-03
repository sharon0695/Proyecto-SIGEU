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

import com.gestion.eventos.Model.ColaboracionModel;
import com.gestion.eventos.Service.IColaboracionService;

@RestController
@RequestMapping ("/rutaCol")
public class ColaboracionController {
    @Autowired IColaboracionService colaboracionService;
    @PostMapping ("/ruta1")
    public ResponseEntity<ColaboracionModel> crearColaboracion(@RequestBody ColaboracionModel colaboracion){
        return new ResponseEntity<>(colaboracionService.guardarColaboracion(colaboracion),HttpStatus.CREATED);
    }
    @GetMapping ("/ruta2")
    public ResponseEntity<List<ColaboracionModel>> listarColaboraciones(){
        return new ResponseEntity<>(colaboracionService.listarColaboraciones(), HttpStatus.OK);
    }
}
