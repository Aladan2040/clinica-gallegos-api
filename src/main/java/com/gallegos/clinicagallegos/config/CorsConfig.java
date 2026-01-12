package com.gallegos.clinicagallegos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // CAMBIO IMPORTANTE:
                // 1. Usamos allowedOriginPatterns en lugar de allowedOrigins para mayor flexibilidad.
                // 2. Permitimos localhost (para tus pruebas).
                // 3. Permitimos cualquier subdominio de Vercel (producción).
                .allowedOriginPatterns("http://localhost:5173", "https://*.vercel.app")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
