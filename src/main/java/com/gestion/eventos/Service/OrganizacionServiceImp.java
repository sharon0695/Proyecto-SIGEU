package com.gestion.eventos.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gestion.eventos.Model.OrganizacionModel;
import com.gestion.eventos.Repository.IOrganizacionRepository;
@Service
public class OrganizacionServiceImp implements IOrganizacionService{
    @Autowired IOrganizacionRepository organizacionRepository;

    @Override
    public OrganizacionModel crearOrganizacion(OrganizacionModel organizacion) {
        if (organizacion.getTelefono() == null || !organizacion.getTelefono().matches("\\d+")) {
            throw new IllegalArgumentException("El teléfono solo debe contener números y no puede estar vacío");
        }
        return organizacionRepository.save(organizacion);
    }

    @Override
    public List<OrganizacionModel> listarORganizaciones() {
        return organizacionRepository.findAll();    
    }

    @Override
    public OrganizacionModel editarOrganizacion(String nit, OrganizacionModel organizacionActualizada) {
        Optional<OrganizacionModel> organizacionExistenteOpt = organizacionRepository.findByNit(nit);

        if (organizacionExistenteOpt.isEmpty()) {
            throw new NoSuchElementException("Organización no encontrada");
        }

        OrganizacionModel organizacionExistente = organizacionExistenteOpt.get();

        if (organizacionActualizada.getNombre() != null) {
            organizacionExistente.setNombre(organizacionActualizada.getNombre());
        }
        if (organizacionActualizada.getRepresentante_legal() != null) {
            organizacionExistente.setRepresentante_legal(organizacionActualizada.getRepresentante_legal());
        }
        if (organizacionActualizada.getUbicacion() != null) {
            organizacionExistente.setUbicacion(organizacionActualizada.getUbicacion());
        }
        if (organizacionActualizada.getTelefono() != null) {
            if (!organizacionActualizada.getTelefono().matches("\\d+")) {
                throw new IllegalArgumentException("El teléfono solo debe contener números");
            }
            organizacionExistente.setTelefono(organizacionActualizada.getTelefono());
        }
        if (organizacionActualizada.getSector_economico() != null) {
            organizacionExistente.setSector_economico(organizacionActualizada.getSector_economico());
        }
        if (organizacionActualizada.getActividad_principal() != null) {
            organizacionExistente.setActividad_principal(organizacionActualizada.getActividad_principal());
        }

        return organizacionRepository.save(organizacionExistente);
    }
}
