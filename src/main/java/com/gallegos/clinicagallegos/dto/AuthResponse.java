package com.gallegos.clinicagallegos.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String token;
    private String rol;
    private String email;
}
