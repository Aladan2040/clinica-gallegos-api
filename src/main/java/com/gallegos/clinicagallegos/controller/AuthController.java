package com.gallegos.clinicagallegos.controller;

import com.gallegos.clinicagallegos.dto.AuthResponse;
import com.gallegos.clinicagallegos.dto.LoginRequest;
import com.gallegos.clinicagallegos.dto.RegistroRequest;
import com.gallegos.clinicagallegos.model.Usuario;
import com.gallegos.clinicagallegos.service.AuthService;
import com.gallegos.clinicagallegos.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthController(AuthService authService, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;

    }

    @PostMapping("/register")
    public ResponseEntity<?> registrarUsuario(@RequestBody RegistroRequest request) {
        try {
            Usuario usuarioRegistrado = authService.registrarPaciente(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado con exito. Id: " + usuarioRegistrado.getUsuarioID());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getContrasena()
                    )
            );
            Usuario usuario = authService.loadUserByEmail(request.getEmail());

            String jwtToken = jwtService.generarToken(usuario);

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(jwtToken)
                    .rol(usuario.getRol().name())
                    .email(usuario.getEmail())
                    .build());
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales invalidas");
        }
    }
}
