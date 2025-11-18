package com.gestion.eventos.Repository;

import com.gestion.eventos.Model.EvaluacionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IEvaluacionRepository extends JpaRepository<EvaluacionModel, Long>{
    List<EvaluacionModel> findByDecision(String decision);
    List<EvaluacionModel> findByCodigoEvento_Codigo(Integer codigoEvento);
}