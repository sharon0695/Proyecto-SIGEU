package com.gestion.eventos.Service;

import java.util.List;

import com.gestion.eventos.Model.OrganizacionModel;

public interface IOrganizacionService {
    OrganizacionModel crearOrganizacion(OrganizacionModel organizacion);
    List<OrganizacionModel> listarORganizaciones();    
    OrganizacionModel editarOrganizacion(String nit, OrganizacionModel organizacionActualizada);
}
