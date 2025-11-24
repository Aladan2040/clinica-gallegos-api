package com.gallegos.clinicagallegos.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String contrasena;
}
