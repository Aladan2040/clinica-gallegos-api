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
        //verificar si el email existe
        if(usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        //Crear y mapear el nuevo usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(request.getNombre());
        nuevoUsuario.setApellido(request.getApellido());
        nuevoUsuario.setEmail(request.getEmail());
        nuevoUsuario.setTelefono(request.getTelefono());

        //Hashing de la contraseña
        String contrasenaHasheada = passwordEncoder.encode(request.getContrasena());
        nuevoUsuario.setContrasenaHash(contrasenaHasheada);
        //Asignar rol por defecto y save
        nuevoUsuario.setRol(Rol.PACIENTE);

        Usuario usuarioRegistrado = usuarioRepository.save(nuevoUsuario);

        String subject = "Bieinvenido/a a la Clínica Dental Gallegos";
        String body = String.format(
                "Hola %s,\n\nGracias por registrarte en la Clínica Dental Gallegos.\n" +
                        "Tu cuenta ha sido creada con éxito. Ya puedes iniciar sesión y agendar tu primera cita.\n" +
                        "Tu email de acceso es: %s\n\n" +
                        "¡Te esperamos!",
                usuarioRegistrado.getNombre(), usuarioRegistrado.getEmail());
        emailService.enviarCorreo(usuarioRegistrado.getEmail(), subject, body);

        return usuarioRegistrado;
    }

    @Override
    public Usuario loadUserByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email"));
    }
}
