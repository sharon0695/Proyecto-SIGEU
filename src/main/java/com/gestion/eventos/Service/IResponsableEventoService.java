package com.gestion.eventos.Service;

import java.util.List;

import com.gestion.eventos.Model.ResponsableEventoModel;

public interface IResponsableEventoService {
    ResponsableEventoModel guardarResponsable(ResponsableEventoModel responsableEvento);
    List<ResponsableEventoModel> listarResponsables();
}
