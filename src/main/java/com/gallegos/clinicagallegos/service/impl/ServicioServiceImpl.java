package com.gallegos.clinicagallegos.service.impl;

import com.gallegos.clinicagallegos.model.Servicio;
import com.gallegos.clinicagallegos.repository.ServicioRepository;
import com.gallegos.clinicagallegos.service.ServicioService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServicioServiceImpl implements ServicioService {
    private final ServicioRepository servicioRepository;

    public ServicioServiceImpl(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    @Override
    public List<Servicio> listarServiciosActivos() {
        return servicioRepository.findByActivoTrue();
    }

    @Override
    public Servicio crearServicio(Servicio servicio) {
        return servicioRepository.save(servicio);
    }

    @Override
    public Optional<Servicio> findById(Integer id) {
        return servicioRepository.findById(id);
    }
}
