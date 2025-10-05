package com.gestion.eventos.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gestion.eventos.Model.OrganizacionModel;
import com.gestion.eventos.Service.IOrganizacionService;

@RestController
@RequestMapping ("/rutaOrg")
public class OrganizacionController {
    @Autowired IOrganizacionService organizacionService;
    @PostMapping ("/ruta1")
    public ResponseEntity<OrganizacionModel> crearOrganizacion(@RequestBody OrganizacionModel organizacion){
        return new ResponseEntity<>(organizacionService.guardarOrganizacion(organizacion),HttpStatus.CREATED);
    }
    @GetMapping ("/ruta2")
    public ResponseEntity<List<OrganizacionModel>> listarOrganizaciones(){
        return new ResponseEntity<>(organizacionService.listarORganizaciones(), HttpStatus.OK);
    }
    @GetMapping("/buscar/{nombre}")
    public ResponseEntity<String> buscarPorNombre(@PathVariable String nombre) {
        String resultado = organizacionService.buscarOrganizacionPorNombre(nombre);
        return ResponseEntity.ok(resultado);
    }
}   
