package com.gallegos.clinicagallegos.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "servicios")
@Data
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "servicio_id")
    private Integer servicioId;

    @Column(name = "nombre", length = 100, unique = true, nullable = false)
    private String nombre;

    @Lob
    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;

    @Column(name = "costo", precision = 10, scale = 2, nullable = false)
    private BigDecimal costo;

    @Column(name = "activo", nullable = false)
    private Boolean activo;
}
