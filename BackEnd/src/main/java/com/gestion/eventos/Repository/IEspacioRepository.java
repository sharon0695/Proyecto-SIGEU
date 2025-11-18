package com.gestion.eventos.Repository;

import com.gestion.eventos.Model.EspacioModel;
import com.gestion.eventos.Model.EvaluacionModel;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IEspacioRepository extends JpaRepository<EspacioModel, String>{

    static List<EvaluacionModel> findByCodigoEvento_Codigo(Integer codigoEvento) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByCodigoEvento_Codigo'");
    }
 //consultas
}
