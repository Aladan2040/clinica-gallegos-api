package com.gallegos.clinicagallegos.repository;

import com.gallegos.clinicagallegos.model.Cita;
import com.gallegos.clinicagallegos.model.EstadoCita;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository  extends JpaRepository<Cita, Integer>, JpaSpecificationExecutor<Cita> {
    @Query(value = "SELECT COUNT(*) FROM citas c " +
            "JOIN servicios s ON c.servicio_id = s.servicio_id " +
            "WHERE c.servicio_id = :servicioId " +
            "AND c.estado IN ('PENDIENTE', 'CONFIRMADA') " +
            "AND :nuevaInicio < DATE_ADD(c.fecha_hora, INTERVAL s.duracion_minutos MINUTE) " +
            "AND :nuevaFin > c.fecha_hora",
            nativeQuery = true)
    Long countOverlappingAppointments(
            @Param("servicioId") Integer servicioId,
            @Param("nuevaInicio") LocalDateTime nuevaInicio,
            @Param("nuevaFin") LocalDateTime nuevaFin
    );

    @Query("SELECT c FROM Cita c " +
            "JOIN FETCH c.servicio s " +
            "JOIN FETCH c.paciente p " +
            "WHERE p.usuarioID = :pacienteId " +
            "ORDER BY c.fechaHora ASC")
    List<Cita> findByPacienteIdOrderAsc(@Param("pacienteId") Integer pacienteId);

    @Query("SELECT c FROM Cita c " +
            "JOIN FETCH c.servicio s " +
            "JOIN FETCH c.paciente p " +
            "WHERE p.usuarioID = :pacienteId " +
            "ORDER BY c.fechaHora DESC")
    List<Cita> findByPacienteIdOrderDesc(@Param("pacienteId") Integer pacienteId);

    Page<Cita> findAll(Pageable pageable);
    Page<Cita> findByEstado(EstadoCita estado, Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM citas c " +
            "JOIN servicios s ON c.servicio_id = s.servicio_id " +
            "WHERE c.servicio_id = :servicioId " +
            "AND c.cita_id <> :citaId " +
            "AND c.estado IN ('PENDIENTE', 'CONFIRMADA') " +
            "AND :nuevaInicio < DATE_ADD(c.fecha_hora, INTERVAL s.duracion_minutos MINUTE) " +
            "AND :nuevaFin > c.fecha_hora",
            nativeQuery = true)
    Long countOverlappingAppointmentsExcluding(
            @Param("citaId") Integer citaId,
            @Param("servicioId") Integer servicioId,
            @Param("nuevaInicio") LocalDateTime nuevaInicio,
            @Param("nuevaFin") LocalDateTime nuevaFin
    );
}
