package com.gallegos.clinicagallegos.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CitaRequest {
    private Integer servicioId;
    private LocalDateTime fechaHora;
    private String notas;
}
