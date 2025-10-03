package com.gestion.eventos.Service;

import java.util.List;

import com.gestion.eventos.Model.ReservacionModel;

public interface IReservacionService {
    ReservacionModel guardarReservacion(ReservacionModel reservacion);
    List<ReservacionModel> listarReservaciones();
}
