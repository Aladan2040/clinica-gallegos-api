package com.gallegos.clinicagallegos.controller;

import com.gallegos.clinicagallegos.model.Servicio;
import com.gallegos.clinicagallegos.service.ServicioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @GetMapping
    public ResponseEntity<List<Servicio>> listarServicios(){
        List<Servicio> servicios = servicioService.listarServiciosActivos();
        return ResponseEntity.ok(servicios);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Servicio> crearServicio(@RequestBody Servicio servicio){
        try {
            Servicio nuevoServicio = servicioService.crearServicio(servicio);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoServicio);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
