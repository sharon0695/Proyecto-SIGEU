package com.gestion.eventos.Repository;

import com.gestion.eventos.Model.ColaboracionModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IColaboracionRepository extends JpaRepository<ColaboracionModel, Integer>{
    List<ColaboracionModel> findAllByCodigoEvento_Codigo(Integer codigo);
    void deleteByCodigoEvento_Codigo(Integer codigo);
    @Query("SELECT COUNT(c) > 0 FROM ColaboracionModel c WHERE c.nit_organizacion.nit = :nit")
    boolean existsByOrganizacionNit(@Param("nit") String nit);
}
