package com.gallegos.clinicagallegos.dto;

import com.gallegos.clinicagallegos.model.EstadoCita;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CitaAdminDTO {
    private Integer citaId;
    private String pacienteNombre; // nombre + apellido
    private String servicioNombre;
    private LocalDateTime fechaHora;
    private EstadoCita estado;
    private String notas;
}
