package com.gallegos.clinicagallegos.repository;

import com.gallegos.clinicagallegos.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Integer> {
    List<Servicio> findByActivoTrue();
}
