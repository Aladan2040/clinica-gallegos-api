package com.gallegos.clinicagallegos.service.impl;

import com.gallegos.clinicagallegos.exception.CitaNoDisponibleException;
import com.gallegos.clinicagallegos.model.Cita;
import com.gallegos.clinicagallegos.model.EstadoCita;
import com.gallegos.clinicagallegos.model.Servicio;
import com.gallegos.clinicagallegos.model.Usuario;
import com.gallegos.clinicagallegos.repository.CitaRepository;
import com.gallegos.clinicagallegos.repository.ServicioRepository;
import com.gallegos.clinicagallegos.repository.UsuarioRepository;
import com.gallegos.clinicagallegos.service.CitaService;
import com.gallegos.clinicagallegos.service.EmailService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;
    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    public CitaServiceImpl(CitaRepository citaRepository, ServicioRepository servicioRepository, UsuarioRepository usuarioRepository, EmailService emailService) {
        this.citaRepository = citaRepository;
        this.servicioRepository = servicioRepository;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
    }

    @Override
    public Cita agendarNuevaCita(Cita nuevaCita, Integer servicioId) {
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        nuevaCita.setServicio(servicio);
        nuevaCita.setEstado(EstadoCita.PENDIENTE);
        nuevaCita.setFechaCreacion(LocalDateTime.now());

        // Calcular fin para validación
        verificarDisponibilidad(servicioId, nuevaCita.getFechaHora(), servicio.getDuracionMinutos());

        Cita citaGuardada = citaRepository.save(nuevaCita);

        // El envío de correo puede fallar sin afectar la transacción principal
        try {
            enviarCorreoAgendamiento(citaGuardada);
        } catch (Exception e) {
            System.err.println("Error enviando correo (no bloqueante): " + e.getMessage());
        }

        return citaGuardada;
    }

    @Override
    @Transactional(readOnly = true) // Optimización para lectura
    public List<Cita> findCitasByPaciente(Usuario paciente, String orden) {
        if ("asc".equalsIgnoreCase(orden)) {
            return citaRepository.findByPacienteIdOrderAsc(paciente.getUsuarioID());
        }
        return citaRepository.findByPacienteIdOrderDesc(paciente.getUsuarioID());
    }

    @Override
    public void verificarDisponibilidad(Integer servicioId, LocalDateTime inicio, int duracionMinutos) {
        LocalDateTime fin = inicio.plusMinutes(duracionMinutos);
        Long solapamientos = citaRepository.countOverlappingAppointments(servicioId, inicio, fin);

        if (solapamientos > 0) {
            throw new CitaNoDisponibleException("El horario solicitado no está disponible.");
        }
    }

    @Override
    public Cita cancelarCita(Integer citaId, Integer usuarioId) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        if (!cita.getPaciente().getUsuarioID().equals(usuarioId)) {
            throw new RuntimeException("No tiene permisos para cancelar esta cita.");
        }

        if (cita.getEstado() == EstadoCita.COMPLETADA || cita.getEstado() == EstadoCita.CANCELADA) {
            throw new RuntimeException("No se puede cancelar una cita completada o ya cancelada.");
        }

        cita.setEstado(EstadoCita.CANCELADA);
        Cita guardada = citaRepository.save(cita);

        try {
            enviarNotificacionCambioEstado(guardada, EstadoCita.PENDIENTE, EstadoCita.CANCELADA);
        } catch (Exception e) {
            System.err.println("Error enviando correo cancelacion: " + e.getMessage());
        }
        return guardada;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Cita> findAllCitasAdmin(EstadoCita estado, Pageable pageable) {
        if (estado != null) {
            return citaRepository.findByEstado(estado, pageable);
        }
        return citaRepository.findAll(pageable);
    }

    @Override
    public Cita cambiarEstadoCitaAdmin(Integer citaId, EstadoCita nuevoEstado) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        EstadoCita estadoAnterior = cita.getEstado();
        if (estadoAnterior != nuevoEstado) {
            cita.setEstado(nuevoEstado);
            Cita guardada = citaRepository.save(cita);

            if (nuevoEstado == EstadoCita.CONFIRMADA || nuevoEstado == EstadoCita.CANCELADA) {
                try {
                    enviarNotificacionCambioEstado(guardada, estadoAnterior, nuevoEstado);
                } catch (Exception e) {
                    System.err.println("Error correo cambio estado: " + e.getMessage());
                }
            }
            return guardada;
        }
        return cita;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Cita> buscarCitasAdmin(String q, EstadoCita estado, Integer servicioId, LocalDateTime desde, LocalDateTime hasta, Pageable pageable) {
        Specification<Cita> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(q)) {
                String likePattern = "%" + q.toLowerCase() + "%";
                Predicate nombrePaciente = cb.like(cb.lower(root.get("paciente").get("nombre")), likePattern);
                Predicate apellidoPaciente = cb.like(cb.lower(root.get("paciente").get("apellido")), likePattern);
                Predicate nombreServicio = cb.like(cb.lower(root.get("servicio").get("nombre")), likePattern);
                predicates.add(cb.or(nombrePaciente, apellidoPaciente, nombreServicio));
            }

            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }

            if (servicioId != null) {
                predicates.add(cb.equal(root.get("servicio").get("servicioId"), servicioId));
            }

            if (desde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaHora"), desde));
            }

            if (hasta != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaHora"), hasta));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return citaRepository.findAll(spec, pageable);
    }

    @Override
    public Cita editarCitaAdmin(Integer citaId, Integer servicioId, LocalDateTime fechaHora, String notas) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        boolean cambioHorario = false;

        if (servicioId != null && !servicioId.equals(cita.getServicio().getServicioId())) {
            Servicio nuevoServicio = servicioRepository.findById(servicioId)
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
            cita.setServicio(nuevoServicio);
            cambioHorario = true;
        }

        if (fechaHora != null && !fechaHora.isEqual(cita.getFechaHora())) {
            cita.setFechaHora(fechaHora);
            cambioHorario = true;
        }

        if (StringUtils.hasText(notas)) {
            cita.setNotas(notas);
        }

        if (cambioHorario) {
            LocalDateTime fin = cita.getFechaHora().plusMinutes(cita.getServicio().getDuracionMinutos());

            Long conflictos = citaRepository.countOverlappingAppointmentsIgnoring(
                    cita.getServicio().getServicioId(),
                    cita.getFechaHora(),
                    fin,
                    citaId
            );

            if (conflictos > 0) {
                throw new CitaNoDisponibleException("El nuevo horario choca con otra cita existente.");
            }
        }

        return citaRepository.save(cita);
    }

    @Override
    public Cita agendarCitaParaUsuario(Integer usuarioId, Integer servicioId, LocalDateTime fechaHora, String notas) {
        Usuario paciente = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        Cita nuevaCita = new Cita();
        nuevaCita.setPaciente(paciente);
        nuevaCita.setFechaHora(fechaHora);
        nuevaCita.setNotas(notas);

        return agendarNuevaCita(nuevaCita, servicioId);
    }

    // --- Métodos Privados de Correo ---

    private void enviarCorreoAgendamiento(Cita cita) {
        String to = cita.getPaciente().getEmail();
        String subject = "Cita Agendada: " + cita.getServicio().getNombre();
        String body = String.format(
                "Hola %s,\n\nTu cita ha sido agendada con éxito.\n" +
                        "Servicio: %s\n" +
                        "Fecha y Hora: %s\n" +
                        "Estado: PENDIENTE\n\n" +
                        "Por favor, preséntate 10 minutos antes.\nGracias.",
                cita.getPaciente().getNombre(),
                cita.getServicio().getNombre(),
                cita.getFechaHora().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
        emailService.enviarCorreo(to, subject, body);
    }

    private void enviarNotificacionCambioEstado(Cita cita, EstadoCita anterior, EstadoCita nuevo) {
        String to = cita.getPaciente().getEmail();
        String servicio = cita.getServicio().getNombre();
        String fechaHora = cita.getFechaHora().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String subject = "Actualización de Cita - Clínica Gallegos";
        String body = "";

        if (nuevo == EstadoCita.CONFIRMADA) {
            subject = "✅ ¡Cita Confirmada!";
            body = String.format("Estimado/a %s,\n\nSu cita para '%s' el %s ha sido CONFIRMADA.",
                    cita.getPaciente().getNombre(), servicio, fechaHora);
        } else if (nuevo == EstadoCita.CANCELADA) {
            subject = "❌ Cita Cancelada";
            body = String.format("Estimado/a %s,\n\nSu cita para '%s' el %s ha sido CANCELADA.",
                    cita.getPaciente().getNombre(), servicio, fechaHora);
        } else {
            return;
        }
        emailService.enviarCorreo(to, subject, body);
    }
}