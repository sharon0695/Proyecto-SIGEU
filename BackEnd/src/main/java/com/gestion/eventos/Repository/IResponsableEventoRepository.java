package com.gestion.eventos.Repository;

import com.gestion.eventos.Model.EventoModel;
import com.gestion.eventos.Model.ResponsableEventoModel;
import com.gestion.eventos.Model.UsuarioModel;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IResponsableEventoRepository extends JpaRepository<ResponsableEventoModel, Integer>{  
    @Query("SELECT COALESCE(MAX(r.consecutivo), 0) FROM ResponsableEventoModel r")
    Integer findMaxConsecutivo();
    List<ResponsableEventoModel> findAllByCodigoEvento_Codigo(Integer codigo);
    void deleteByCodigoEvento_Codigo(Integer codigo);
    Optional<ResponsableEventoModel> findByIdUsuarioAndCodigoEvento(UsuarioModel id_usuario, EventoModel codigoEvento);
}
