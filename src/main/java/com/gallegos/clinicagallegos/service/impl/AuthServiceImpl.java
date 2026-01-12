package com.gallegos.clinicagallegos.service.impl;

import com.gallegos.clinicagallegos.dto.RegistroRequest;
import com.gallegos.clinicagallegos.model.Rol;
import com.gallegos.clinicagallegos.model.Usuario;
import com.gallegos.clinicagallegos.repository.UsuarioRepository;
import com.gallegos.clinicagallegos.service.AuthService;
import com.gallegos.clinicagallegos.service.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    public Usuario registrarPaciente(RegistroRequest request) {
        if(usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(request.getNombre());
        nuevoUsuario.setApellido(request.getApellido());
        nuevoUsuario.setEmail(request.getEmail());
        nuevoUsuario.setTelefono(request.getTelefono());

        String contrasenaHasheada = passwordEncoder.encode(request.getContrasena());
        nuevoUsuario.setContrasenaHash(contrasenaHasheada);
        nuevoUsuario.setRol(Rol.PACIENTE);

        Usuario usuarioRegistrado = usuarioRepository.save(nuevoUsuario);

        // Notificación
        try {
            String subject = "Bienvenido/a a la Clínica Dental Gallegos";
            String body = String.format(
                    "Hola %s,\n\nGracias por registrarte.\nTu cuenta ha sido creada con éxito.\n¡Te esperamos!",
                    usuarioRegistrado.getNombre());
            emailService.enviarCorreo(usuarioRegistrado.getEmail(), subject, body);
        } catch (Exception e) {
            System.err.println("Error correo bienvenida: " + e.getMessage());
        }

        return usuarioRegistrado;
    }

    @Override
    public Usuario loadUserByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
    }
}