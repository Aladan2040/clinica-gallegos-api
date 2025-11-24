package com.gallegos.clinicagallegos.dto;

import lombok.Data;

@Data
public class RegistroRequest {
    private String nombre;
    private String apellido;
    private String email;
    private String contrasena;
    private String telefono;
}
