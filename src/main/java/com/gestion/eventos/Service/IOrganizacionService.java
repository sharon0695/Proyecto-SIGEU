package com.gestion.eventos.Service;

import java.util.List;

import com.gestion.eventos.Model.OrganizacionModel;

public interface IOrganizacionService {
    OrganizacionModel guardarOrganizacion(OrganizacionModel organizacion);
    List<OrganizacionModel> listarORganizaciones();
    String buscarOrganizacionPorNombre(String nombre);
}
