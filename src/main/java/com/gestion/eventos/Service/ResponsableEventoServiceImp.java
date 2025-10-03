package com.gestion.eventos.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gestion.eventos.Model.ResponsableEventoModel;
import com.gestion.eventos.Repository.IResponsableEventoRepository;
@Service
public class ResponsableEventoServiceImp implements IResponsableEventoService{
    @Autowired IResponsableEventoRepository responsableEventoRepository;

    @Override
    public ResponsableEventoModel guardarResponsable(ResponsableEventoModel responsableEvento) {
        return responsableEventoRepository.save(responsableEvento);    
    }

    @Override
    public List<ResponsableEventoModel> listarResponsables() {
        return responsableEventoRepository.findAll();    
    }

}
