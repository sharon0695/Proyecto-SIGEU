package com.gestion.eventos.Controller;

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

import com.gestion.eventos.Model.OrganizacionModel;
import com.gestion.eventos.Service.IOrganizacionService;

@RestController
@RequestMapping ("/OrganizacionExterna")
public class OrganizacionController {
    @Autowired IOrganizacionService organizacionService;
    @PostMapping("/registrar")
    public ResponseEntity<OrganizacionModel> crearOrganizacion(@RequestBody OrganizacionModel organizacion){
            OrganizacionModel orgGuardada = organizacionService.crearOrganizacion(organizacion);
            return new ResponseEntity<>(orgGuardada, HttpStatus.CREATED); 
    }

    @GetMapping ("/visualizacion")
    public ResponseEntity<List<OrganizacionModel>> listarOrganizaciones(){
        return new ResponseEntity<>(organizacionService.listarORganizaciones(), HttpStatus.OK);
    }
    
    //Editar Informacion
   @PutMapping("/editar/{nit}")
    public ResponseEntity<OrganizacionModel> editarOrganizacion(@PathVariable String nit,
                                                @RequestBody OrganizacionModel organizacionActualizada) {
            OrganizacionModel orgActualizada = organizacionService.editarOrganizacion(nit, organizacionActualizada);
            return new ResponseEntity<>(orgActualizada, HttpStatus.OK);
    }
}   
