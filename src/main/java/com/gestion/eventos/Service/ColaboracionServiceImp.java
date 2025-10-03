package com.gestion.eventos.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gestion.eventos.Model.ColaboracionModel;
import com.gestion.eventos.Repository.IColaboracionRepository;
@Service
public class ColaboracionServiceImp implements IColaboracionService{
    @Autowired IColaboracionRepository colaboracionRepository;

    @Override
    public ColaboracionModel guardarColaboracion(ColaboracionModel colaboracion){

        return colaboracionRepository.save(colaboracion);
    }

    @Override
    public List<ColaboracionModel> listarColaboraciones() {
       return colaboracionRepository.findAll();
    }
}
