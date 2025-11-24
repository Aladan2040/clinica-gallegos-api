package com.gallegos.clinicagallegos.service;

import com.gallegos.clinicagallegos.model.Cita;
import com.gallegos.clinicagallegos.model.EstadoCita;
import com.gallegos.clinicagallegos.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface CitaService {

    //Metodo para registrar una nueva cita, incluyendo verificacion en caso de conlicto
    Cita agendarNuevaCita(Cita cita, Integer servicioId);

    //Metodo para obtener las citas de un paciente
    List<Cita> findCitasByPaciente(Usuario paciente, String orden);

    //Metodo auxiliar para la logica de agendamiento
    void verificarDisponibilidad(Integer servicioId, LocalDateTime inicio, int duracionMinutos);

    Cita cancelarCita(Integer citaId, Integer usuarioId);

    Page<Cita> findAllCitasAdmin(EstadoCita estado, Pageable pageable);
    Cita cambiarEstadoCitaAdmin(Integer citaId, EstadoCita nuevoEstado);

    Page<Cita> buscarCitasAdmin(String q, EstadoCita estado, Integer servicioId, LocalDateTime desde, LocalDateTime hasta, Pageable pageable);
    Cita editarCitaAdmin(Integer citaId, Integer servicioId, LocalDateTime fechaHora, String notas);

    // Método para que el admin agende citas para un usuario específico
    Cita agendarCitaParaUsuario(Integer pacienteId, Integer servicioId, LocalDateTime fechaHora, String notas);
}
