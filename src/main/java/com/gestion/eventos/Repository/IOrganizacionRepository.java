package com.gestion.eventos.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestion.eventos.Model.OrganizacionModel;

public interface IOrganizacionRepository extends JpaRepository<OrganizacionModel, String>{
    Optional<OrganizacionModel> findByNit(String nit);
}
