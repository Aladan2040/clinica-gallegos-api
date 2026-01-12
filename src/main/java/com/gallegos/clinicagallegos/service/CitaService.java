package com.gallegos.clinicagallegos.service;

import com.gallegos.clinicagallegos.model.Cita;
import com.gallegos.clinicagallegos.model.EstadoCita;
import com.gallegos.clinicagallegos.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface CitaService {

        /**
         * Agenda una nueva cita para un paciente, verificando disponibilidad.
         */
        Cita agendarNuevaCita(Cita cita, Integer servicioId);

        /**
         * Obtiene la lista de citas de un paciente específico.
         * Se puede especificar un orden ('asc' o 'desc').
         */
        List<Cita> findCitasByPaciente(Usuario paciente, String orden);

        /**
         * Metodo auxiliar para la logica de agendamiento.
         * Verifica conflictos de horario antes de guardar.
         */
        void verificarDisponibilidad(Integer servicioId, LocalDateTime inicio, int duracionMinutos);

        /**
         * Cancela una cita existente si cumple con las reglas de negocio (dueño, estado).
         */
        Cita cancelarCita(Integer citaId, Integer usuarioId);

        /**
         * Lista citas para el panel de administración con soporte de paginación.
         * Filtra por estado opcionalmente.
         */
        Page<Cita> findAllCitasAdmin(EstadoCita estado, Pageable pageable);

        /**
         * Permite a un administrador cambiar el estado de cualquier cita.
         */
        Cita cambiarEstadoCitaAdmin(Integer citaId, EstadoCita nuevoEstado);

        /**
         * Búsqueda avanzada para el panel de administración.
         * Permite buscar por texto (nombre paciente/servicio), estado, servicio y rango de fechas.
         */
        Page<Cita> buscarCitasAdmin(String q, EstadoCita estado, Integer servicioId, LocalDateTime desde, LocalDateTime hasta, Pageable pageable);

        /**
         * Permite a un administrador editar los detalles de una cita existente.
         */
        Cita editarCitaAdmin(Integer citaId, Integer servicioId, LocalDateTime fechaHora, String notas);
}
