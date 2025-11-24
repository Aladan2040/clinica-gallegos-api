package com.gallegos.clinicagallegos.service;

import com.gallegos.clinicagallegos.dto.RegistroRequest;
import com.gallegos.clinicagallegos.model.Usuario;

public interface AuthService {
    Usuario registrarPaciente(RegistroRequest request);
    Usuario loadUserByEmail(String email);
}
