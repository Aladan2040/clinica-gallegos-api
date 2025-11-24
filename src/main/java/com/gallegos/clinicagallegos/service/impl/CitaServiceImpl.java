package com.gallegos.clinicagallegos.service.impl;

import com.gallegos.clinicagallegos.model.Cita;
import com.gallegos.clinicagallegos.model.EstadoCita;
import com.gallegos.clinicagallegos.model.Servicio;
import com.gallegos.clinicagallegos.model.Usuario;
import com.gallegos.clinicagallegos.repository.CitaRepository;
import com.gallegos.clinicagallegos.repository.ServicioRepository;
import com.gallegos.clinicagallegos.repository.UsuarioRepository;
import com.gallegos.clinicagallegos.service.CitaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CitaServiceImpl implements CitaService {
    private final CitaRepository citaRepository;
    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;

    //Inyeccion de dependencias a traves del constructor
    public CitaServiceImpl(CitaRepository citaRepository, ServicioRepository servicioRepository, UsuarioRepository usuarioRepository) {
        this.citaRepository = citaRepository;
        this.servicioRepository = servicioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Cita agendarNuevaCita(Cita nuevaCita, Integer servicioId){
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        nuevaCita.setServicio(servicio);

        // Establecer estado por defecto y fecha de creación
        nuevaCita.setEstado(EstadoCita.PENDIENTE);
        nuevaCita.setFechaCreacion(LocalDateTime.now());

        verificarDisponibilidad(servicioId,nuevaCita.getFechaHora(), servicio.getDuracionMinutos());
        return citaRepository.save(nuevaCita);
    }

    @Override
    public void verificarDisponibilidad(Integer servicioId, LocalDateTime inicio, int duracionMinutos) {
        LocalDateTime fin = inicio.plusMinutes(duracionMinutos);
        Long conflictos = citaRepository.countOverlappingAppointments(servicioId, inicio, fin);
        if (conflictos > 0) {
            throw new RuntimeException("La cita no puede ser agendada debido a un conflicto de horario.");
        }
    }

    @Override
    public Cita cancelarCita(Integer citaId, Integer usuarioId) {
        //Buscar cita por Id
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        //Verificar que el usuario tenga asignada dicha cita
        if(!cita.getPaciente().getUsuarioID().equals(usuarioId)){
            throw new RuntimeException("Acceso denegado. No es el dueño de la cita");
        }

        //Verificar estado de la cita(solo se pueden cancelar citas en estado pendiente/confirmadas)
        if (cita.getEstado() == EstadoCita.COMPLETADA || cita.getEstado() == EstadoCita.CANCELADA) {
            throw new RuntimeException("Solo se cancelan citas confirmadas o pendientes");
        }
        cita.setEstado(EstadoCita.CANCELADA);
        return citaRepository.save(cita);
    }

    @Override
    public List<Cita> findCitasByPaciente(Usuario paciente, String orden) {
        String ord = (orden == null ? "asc" : orden.trim().toLowerCase());
        if (!ord.equals("asc") && !ord.equals("desc")) {
            ord = "asc"; // valor por defecto seguro
        }
        return ord.equals("asc") ?
                citaRepository.findByPacienteIdOrderAsc(paciente.getUsuarioID()) :
                citaRepository.findByPacienteIdOrderDesc(paciente.getUsuarioID());
    }

    @Override
    public Page<Cita> findAllCitasAdmin(EstadoCita estado, Pageable pageable) {
        if (estado == null) {
            return citaRepository.findAll(pageable);
        }
        return citaRepository.findByEstado(estado, pageable);
    }

    @Override
    public Cita cambiarEstadoCitaAdmin(Integer citaId, EstadoCita nuevoEstado) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        cita.setEstado(nuevoEstado);
        return citaRepository.save(cita);
    }

    @Override
    public Page<Cita> buscarCitasAdmin(String q, EstadoCita estado, Integer servicioId, LocalDateTime desde, LocalDateTime hasta, Pageable pageable) {
        Specification<Cita> spec = Specification.where(null);
        if (estado != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estado"), estado));
        }
        if (servicioId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("servicio").get("servicioId"), servicioId));
        }
        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("paciente").get("nombre")), like),
                    cb.like(cb.lower(root.get("paciente").get("apellido")), like),
                    cb.like(cb.lower(root.get("paciente").get("email")), like),
                    cb.like(cb.lower(root.get("servicio").get("nombre")), like)
            ));
        }
        if (desde != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fechaHora"), desde));
        }
        if (hasta != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("fechaHora"), hasta));
        }
        return citaRepository.findAll(spec, pageable);
    }

    @Override
    public Cita editarCitaAdmin(Integer citaId, Integer servicioId, LocalDateTime fechaHora, String notas) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        if (servicioId != null) {
            Servicio servicio = servicioRepository.findById(servicioId)
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
            cita.setServicio(servicio);
        }
        if (fechaHora != null) {
            if (fechaHora.isBefore(LocalDateTime.now())) {
                throw new RuntimeException("La fecha/hora debe ser futura");
            }
            // Verificar conflictos excluyendo esta cita
            Servicio servicioActual = cita.getServicio();
            int duracion = servicioActual.getDuracionMinutos();
            LocalDateTime fin = fechaHora.plusMinutes(duracion);
            Long conflictos = citaRepository.countOverlappingAppointmentsExcluding(cita.getCitaId(), servicioActual.getServicioId(), fechaHora, fin);
            if (conflictos != null && conflictos > 0) {
                throw new RuntimeException("Conflicto de horario con otra cita");
            }
            cita.setFechaHora(fechaHora);
        }
        if (notas != null) {
            cita.setNotas(notas);
        }
        return citaRepository.save(cita);
    }

    @Override
    public Cita agendarCitaParaUsuario(Integer pacienteId, Integer servicioId, LocalDateTime fechaHora, String notas) {
        // Buscar el paciente
        Usuario paciente = usuarioRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        // Buscar el servicio
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        // Validar que la fecha sea futura
        if (fechaHora.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("La fecha/hora debe ser futura");
        }

        // Verificar disponibilidad
        verificarDisponibilidad(servicioId, fechaHora, servicio.getDuracionMinutos());

        // Crear la nueva cita
        Cita nuevaCita = new Cita();
        nuevaCita.setPaciente(paciente);
        nuevaCita.setServicio(servicio);
        nuevaCita.setFechaHora(fechaHora);
        nuevaCita.setNotas(notas);
        nuevaCita.setEstado(EstadoCita.PENDIENTE);
        nuevaCita.setFechaCreacion(LocalDateTime.now());

        return citaRepository.save(nuevaCita);
    }
}
