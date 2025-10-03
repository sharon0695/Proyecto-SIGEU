package com.gestion.eventos.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestion.eventos.Model.UsuarioModel;

public interface IUsuarioRepository extends JpaRepository<UsuarioModel, Integer>{
    Optional<UsuarioModel> findByCorreoInstitucional(String correo_institucional);
    Optional<UsuarioModel> findByIdentificacion(Integer identificacion);
}
