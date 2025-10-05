package com.gestion.eventos.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gestion.eventos.Model.OrganizacionModel;
import com.gestion.eventos.Repository.IOrganizacionRepository;

@Service
public class OrganizacionServiceImp implements IOrganizacionService {

    @Autowired 
    IOrganizacionRepository organizacionRepository;

    @Override
    public OrganizacionModel guardarOrganizacion(OrganizacionModel organizacion) {
        return organizacionRepository.save(organizacion);    
    }

    @Override
    public List<OrganizacionModel> listarORganizaciones() {
        return organizacionRepository.findAll();    
    }

    @Override
    public String buscarOrganizacionPorNombre(String nombre) {
        Optional<OrganizacionModel> organizacion = organizacionRepository.findByNombre(nombre);
        
        if (organizacion.isPresent()) {
            return "Organización encontrada: " + organizacion.get().getNombre();
        } else {
            return "La organización '" + nombre + "' no existe.";
        }
    }
}
