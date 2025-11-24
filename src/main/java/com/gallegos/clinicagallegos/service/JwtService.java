package com.gallegos.clinicagallegos.service;

import com.gallegos.clinicagallegos.model.Usuario;

public interface JwtService {
    String generarToken(Usuario usuario);
    String extraerUsername(String token);
    boolean esTokenValido(String token, Usuario usuario);
}
