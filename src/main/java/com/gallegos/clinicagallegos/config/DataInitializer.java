package com.gallegos.clinicagallegos.config;

import com.gallegos.clinicagallegos.model.Rol;
import com.gallegos.clinicagallegos.model.Servicio;
import com.gallegos.clinicagallegos.model.Usuario;
import com.gallegos.clinicagallegos.repository.ServicioRepository;
import com.gallegos.clinicagallegos.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {
    // --- DATOS DEL ADMINISTRADOR ---
    private static final String ADMIN_EMAIL = "admin@gallegos.com";
    private static final String ADMIN_PASSWORD_PLAINTEXT = "SuperAdmin2025!";

    @Bean
    public CommandLineRunner initData(UsuarioRepository usuarioRepository,
                                      ServicioRepository servicioRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. INICIALIZAR USUARIO ADMIN
            if (!usuarioRepository.existsByEmail(ADMIN_EMAIL)) {
                System.out.println("--- Creando usuario administrador inicial... ---");

                Usuario admin = new Usuario();
                admin.setNombre("Super");
                admin.setApellido("Admin");
                admin.setEmail(ADMIN_EMAIL);
                admin.setTelefono("999999999");
                admin.setRol(Rol.ADMIN);
                admin.setContrasenaHash(passwordEncoder.encode(ADMIN_PASSWORD_PLAINTEXT));

                usuarioRepository.save(admin);
                System.out.println("✅ Administrador creado exitosamente.");
            }

            // 2. INICIALIZAR SERVICIOS (Desde tu SQL)
            if (servicioRepository.count() == 0) {
                System.out.println("--- La tabla de Servicios está vacía. Insertando datos iniciales... ---");

                List<Servicio> servicios = Arrays.asList(
                        crearServicio("Limpieza Dental Básica",
                                "Remoción de placa, sarro y pulido dental.",
                                45, new BigDecimal("50.00")),

                        crearServicio("Control Odontológico General",
                                "Revisión completa, evaluación de salud bucal y diagnóstico.",
                                30, new BigDecimal("25.00")),

                        crearServicio("Blanqueamiento Dental LED",
                                "Tratamiento de blanqueamiento profesional con luz LED.",
                                90, new BigDecimal("250.00")),

                        crearServicio("Endodoncia (Tratamiento de Conducto)",
                                "Tratamiento para salvar un diente dañado o infectado.",
                                120, new BigDecimal("350.00")),

                        crearServicio("Extracción Simple",
                                "Remoción de piezas dentales sin complicaciones quirúrgicas.",
                                60, new BigDecimal("80.00")),

                        crearServicio("Otros / Servicio No Listado",
                                "Solicitud especial que requiere revisión por parte del especialista.",
                                30, new BigDecimal("0.00"))
                );

                servicioRepository.saveAll(servicios);
                System.out.println("✅ " + servicios.size() + " Servicios insertados exitosamente.");
            } else {
                System.out.println("--- Los servicios ya existen en la BD. No se requiere acción. ---");
            }
        };
    }

    // Método auxiliar para crear objetos Servicio más limpio
    private Servicio crearServicio(String nombre, String descripcion, Integer duracion, BigDecimal costo) {
        Servicio s = new Servicio();
        s.setNombre(nombre);
        s.setDescripcion(descripcion);
        s.setDuracionMinutos(duracion);
        s.setCosto(costo);
        s.setActivo(true); // Asumimos que el default es true
        return s;
    }
}
