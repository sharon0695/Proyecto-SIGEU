package com.gestion.eventos.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gestion.eventos.Model.ReservacionModel;
import com.gestion.eventos.Repository.IReservacionRepository;
@Service
public class ReservacionServiceImp implements IReservacionService{
    @Autowired IReservacionRepository reservacionRepository;

    @Override
    public ReservacionModel guardarReservacion(ReservacionModel reservacion) {
        return reservacionRepository.save(reservacion);
    }

    @Override
    public List<ReservacionModel> listarReservaciones() {
        return reservacionRepository.findAll();    
    }

}
