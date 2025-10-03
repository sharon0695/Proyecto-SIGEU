package com.gestion.eventos.Service;

import java.util.List;

import com.gestion.eventos.Model.ColaboracionModel;

public interface IColaboracionService {
    ColaboracionModel guardarColaboracion(ColaboracionModel colaboracion);
    List<ColaboracionModel> listarColaboraciones();
}
