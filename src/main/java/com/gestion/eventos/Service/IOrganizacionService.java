package com.gestion.eventos.Service;

import java.util.List;

import com.gestion.eventos.Model.OrganizacionModel;

public interface IOrganizacionService {
    OrganizacionModel guardarOrganizacion(OrganizacionModel organizacion);
    List<OrganizacionModel> listarOrganizaciones();
    String buscarOrganizacionPorNombre(String nombre);
    OrganizacionModel editarOrganizacion(String nit, OrganizacionModel organizacionActualizada);
}
