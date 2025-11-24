package com.gallegos.clinicagallegos.service;

import com.gallegos.clinicagallegos.model.Servicio;

import java.util.List;
import java.util.Optional;

public interface ServicioService {
    List<Servicio> listarServiciosActivos();
    Servicio crearServicio(Servicio servicio);
    Optional<Servicio> findById(Integer id);
}
