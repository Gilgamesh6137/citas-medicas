package com.damian.citas.repository;

import com.damian.citas.entity.Cita;
import com.damian.citas.enums.EstadoCita;
import com.damian.commons.enums.EstadoRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByEstadoRegistro(EstadoRegistro estadoRegistro);
    boolean existsByIdPacienteAndEstadoRegistroAndEstadoCitaIn(Long idPaciente, EstadoRegistro estadoRegistro, List<EstadoCita> estadosCita);
    boolean existsByIdMedicoAndEstadoRegistroAndEstadoCitaIn(Long idMedico, EstadoRegistro estadoRegistro, List<EstadoCita> estadosCita);
}
