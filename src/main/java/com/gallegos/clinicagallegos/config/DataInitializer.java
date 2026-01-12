package com.gallegos.clinicagallegos.config;

import com.gallegos.clinicagallegos.model.Rol;
import com.gallegos.clinicagallegos.model.Usuario;
import com.gallegos.clinicagallegos.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    private static final String ADMIN_EMAIL = "admin@gallegos.com";
    private static final String ADMIN_PASSWORD_PLAINTEXT = "SuperAdmin2025!";

    @Bean
    public CommandLineRunner initAdminUser(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Verificar si el administrador ya existe
            if (!usuarioRepository.existsByEmail(ADMIN_EMAIL)) {

                System.out.println("Creando usuario administrador inicial...");

                Usuario admin = new Usuario();
                admin.setNombre("Super");
                admin.setApellido("Admin");
                admin.setEmail(ADMIN_EMAIL);
                admin.setTelefono("999999999");
                admin.setRol(Rol.ADMIN);

                // 2. Hashear la contraseña de forma segura
                String hashedPassword = passwordEncoder.encode(ADMIN_PASSWORD_PLAINTEXT);
                admin.setContrasenaHash(hashedPassword);

                // 3. Guardar en la base de datos
                usuarioRepository.save(admin);
                System.out.println("Administrador creado exitosamente.");
            }
        };
    }
}
