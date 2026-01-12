package com.gallegos.clinicagallegos.controller;

import com.gallegos.clinicagallegos.dto.CitaAdminDTO;
import com.gallegos.clinicagallegos.dto.CitaRequest;
import com.gallegos.clinicagallegos.exception.CitaNoDisponibleException;
import com.gallegos.clinicagallegos.model.Cita;
import com.gallegos.clinicagallegos.model.EstadoCita;
import com.gallegos.clinicagallegos.model.Usuario;
import com.gallegos.clinicagallegos.service.CitaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {
    private final CitaService citaService;
    private static final DateTimeFormatter[] ACCEPTED_FORMATS = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    };

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    // Post para agendar una cita (PACIENTE o ADMIN autenticado)
    @PostMapping
    public ResponseEntity<?> agendarCita(@RequestBody CitaRequest request, Authentication authentication) {
        try {
            Usuario paciente = (Usuario) authentication.getPrincipal();
            // Permitir solo PACIENTE (no ADMIN) en iteración 2
            if (paciente.getRol() != null && paciente.getRol().name().equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("El administrador no agenda citas");
            }
            Cita nuevaCita = new Cita();
            nuevaCita.setPaciente(paciente);
            nuevaCita.setFechaHora(request.getFechaHora());
            nuevaCita.setNotas(request.getNotas());
            Cita citaGuardada = citaService.agendarNuevaCita(nuevaCita, request.getServicioId());
            return ResponseEntity.status(HttpStatus.CREATED).body(citaGuardada);
        } catch (CitaNoDisponibleException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al agendar cita: " + e.getMessage());
        }
    }

    // Get para listar citas del usuario autenticado
    @GetMapping("/mis-citas")
    public ResponseEntity<List<Cita>> listarMisCitas(Authentication authentication,
                                                     @RequestParam(name = "orden", required = false) String orden) {
        Usuario paciente = (Usuario) authentication.getPrincipal();
        List<Cita> misCitas = citaService.findCitasByPaciente(paciente, orden);
        return ResponseEntity.ok(misCitas);
    }

    // Put para cancelar una cita propia
    @PutMapping("/{citaId}/cancelar")
    public ResponseEntity<?> cancelarCita(@PathVariable Integer citaId, Authentication authentication) {
        try {
            Usuario paciente = (Usuario) authentication.getPrincipal();
            Integer usuarioId = paciente.getUsuarioID();
            if (paciente.getRol() != null && paciente.getRol().name().equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("El administrador no cancela sus propias citas");
            }
            Cita citaCancelada = citaService.cancelarCita(citaId, usuarioId);
            return ResponseEntity.ok(citaCancelada);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Get para búsqueda avanzada de citas (ADMIN)
    @GetMapping("/admin")
    public ResponseEntity<?> buscarCitasAdmin(Authentication authentication,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) String estado,
                                              @RequestParam(required = false) String q,
                                              @RequestParam(required = false) Integer servicioId,
                                              @RequestParam(required = false) String desde,
                                              @RequestParam(required = false) String hasta) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        if (usuario.getRol() == null || !usuario.getRol().name().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }
        Pageable pageable = PageRequest.of(page, size);
        EstadoCita estadoEnum = null;
        if (estado != null && !estado.isBlank()) {
            try { estadoEnum = EstadoCita.valueOf(estado.toUpperCase()); } catch (IllegalArgumentException ignored) {}
        }
        LocalDateTime desdeDt = null;
        LocalDateTime hastaDt = null;
        try { if (desde != null) desdeDt = LocalDateTime.parse(desde); } catch (Exception ignored) {}
        try { if (hasta != null) hastaDt = LocalDateTime.parse(hasta); } catch (Exception ignored) {}
        Page<Cita> pagina = citaService.buscarCitasAdmin(q, estadoEnum, servicioId, desdeDt, hastaDt, pageable);
        Page<CitaAdminDTO> dtoPage = pagina.map(c -> new CitaAdminDTO(
                c.getCitaId(),
                c.getPaciente().getNombre() + " " + c.getPaciente().getApellido(),
                c.getServicio().getNombre(),
                c.getFechaHora(),
                c.getEstado(),
                c.getNotas()
        ));
        return ResponseEntity.ok(dtoPage);
    }

    // Patch para cambiar el estado de una cita (ADMIN)
    @PatchMapping("/admin/{citaId}/estado")
    public ResponseEntity<?> cambiarEstadoAdmin(Authentication authentication,
                                                 @PathVariable Integer citaId,
                                                 @RequestBody String nuevoEstado) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        if (usuario.getRol() == null || !usuario.getRol().name().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }
        try {
            EstadoCita estadoCita = EstadoCita.valueOf(nuevoEstado.replace("\"", "").trim().toUpperCase());
            Cita actualizada = citaService.cambiarEstadoCitaAdmin(citaId, estadoCita);
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Estado inválido");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Put para editar una cita (ADMIN)
    @PutMapping("/admin/{citaId}")
    public ResponseEntity<?> editarCitaAdmin(Authentication authentication,
                                               @PathVariable Integer citaId,
                                               @RequestBody EditarCitaRequest body) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        if (usuario.getRol() == null || !usuario.getRol().name().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }
        try {
            // Parsear fechaHora de forma tolerante
            LocalDateTime fecha = parseFechaHora(body.getFechaHora());
            Cita editada = citaService.editarCitaAdmin(citaId, body.getServicioId(), fecha, body.getNotas());
            if (body.getEstado() != null && !body.getEstado().isBlank()) {
                try {
                    EstadoCita nuevo = EstadoCita.valueOf(body.getEstado().toUpperCase());
                    editada = citaService.cambiarEstadoCitaAdmin(citaId, nuevo);
                } catch (IllegalArgumentException ignored) {
                    // si el estado no es válido, simplemente ignorar el cambio
                }
            }
            return ResponseEntity.ok(new CitaAdminDTO(
                    editada.getCitaId(),
                    editada.getPaciente().getNombre() + " " + editada.getPaciente().getApellido(),
                    editada.getServicio().getNombre(),
                    editada.getFechaHora(),
                    editada.getEstado(),
                    editada.getNotas()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // DTO interno para edición (simplifica iteración 2 sin archivo separado)
    public static class EditarCitaRequest {
        private Integer servicioId; private String fechaHora; private String notas; private String estado;
        public Integer getServicioId() { return servicioId; }
        public void setServicioId(Integer servicioId) { this.servicioId = servicioId; }
        public String getFechaHora() { return fechaHora; }
        public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }
        public String getNotas() { return notas; }
        public void setNotas(String notas) { this.notas = notas; }
        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
    }

    // DTO para agendar cita por admin
    public static class AgendarCitaAdminRequest {
        private Integer pacienteId;
        private Integer servicioId;
        private String fechaHora;
        private String notas;

        public Integer getPacienteId() { return pacienteId; }
        public void setPacienteId(Integer pacienteId) { this.pacienteId = pacienteId; }
        public Integer getServicioId() { return servicioId; }
        public void setServicioId(Integer servicioId) { this.servicioId = servicioId; }
        public String getFechaHora() { return fechaHora; }
        public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }
        public String getNotas() { return notas; }
        public void setNotas(String notas) { this.notas = notas; }
    }

    // Endpoint para que el admin pueda agendar una cita para un usuario
    @PostMapping("/admin/agendar")
    public ResponseEntity<?> agendarCitaAdmin(Authentication authentication, @RequestBody AgendarCitaAdminRequest body) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        if (usuario.getRol() == null || !usuario.getRol().name().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }
        try {
            LocalDateTime fecha = parseFechaHora(body.getFechaHora());
            Cita creada = citaService.agendarCitaParaUsuario(body.getPacienteId(), body.getServicioId(), fecha, body.getNotas());
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private LocalDateTime parseFechaHora(String s) {
        if (s == null) return null;
        String val = s.trim();
        for (DateTimeFormatter fmt : ACCEPTED_FORMATS) {
            try {
                return LocalDateTime.parse(val, fmt);
            } catch (DateTimeParseException ignored) {}
        }
        // Intentar normalizar agregando segundos si faltan
        try {
            if (val.length() == 16) { // yyyy-MM-ddTHH:mm
                val = val + ":00";
                return LocalDateTime.parse(val, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            }
        } catch (Exception ignored) {}
        return null;
    }
}
