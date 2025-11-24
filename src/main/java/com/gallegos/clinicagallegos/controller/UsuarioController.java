package com.gallegos.clinicagallegos.controller;

import com.gallegos.clinicagallegos.model.Rol;
import com.gallegos.clinicagallegos.model.Usuario;
import com.gallegos.clinicagallegos.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Get para obtener lista de pacientes (solo ADMIN)
    @GetMapping("/pacientes")
    public ResponseEntity<?> obtenerPacientes(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        if (usuario.getRol() == null || !usuario.getRol().name().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }

        List<Usuario> pacientes = usuarioRepository.findAll()
                .stream()
                .filter(u -> u.getRol() == Rol.PACIENTE)
                .collect(Collectors.toList());

        // Crear DTOs simplificados para enviar solo la info necesaria
        List<PacienteDTO> pacientesDTO = pacientes.stream()
                .map(p -> new PacienteDTO(
                        p.getUsuarioID(),
                        p.getNombre(),
                        p.getApellido(),
                        p.getEmail(),
                        p.getTelefono()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(pacientesDTO);
    }

    // DTO para enviar información de pacientes
    public static class PacienteDTO {
        private Integer usuarioId;
        private String nombre;
        private String apellido;
        private String email;
        private String telefono;

        public PacienteDTO(Integer usuarioId, String nombre, String apellido, String email, String telefono) {
            this.usuarioId = usuarioId;
            this.nombre = nombre;
            this.apellido = apellido;
            this.email = email;
            this.telefono = telefono;
        }

        public Integer getUsuarioId() { return usuarioId; }
        public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getApellido() { return apellido; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
    }
}

