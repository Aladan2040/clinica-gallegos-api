package com.gallegos.clinicagallegos.service;

import com.gallegos.clinicagallegos.dto.RegistroRequest;
import com.gallegos.clinicagallegos.model.Usuario;

public interface AuthService {
    /**
     * Registra un nuevo paciente en el sistema y envía correo de bienvenida.
     */
    Usuario registrarPaciente(RegistroRequest request);

    /**
     * Carga un usuario por su correo electrónico.
     */
    Usuario loadUserByEmail(String email);
}
