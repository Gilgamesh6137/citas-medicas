package com.damian.pacientes.repository;

import com.damian.pacientes.entity.Paciente;
import com.damian.commons.enums.EstadoRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    boolean existsByEmailIgnoreCaseAndEstadoRegistro(String email, EstadoRegistro estadoregistro);
    boolean existsByEmailIgnoreCaseAndEstadoRegistroAndIdNot(String email, EstadoRegistro estadoregistro, Long id);

    boolean existsByTelefonoAndEstadoRegistro(String telefono, EstadoRegistro estadoregistro);
    boolean existsByTelefonoAndEstadoRegistroAndIdNot(String telefono, EstadoRegistro estadoregistro, Long id);

    List<Paciente> findByEstadoRegistro(EstadoRegistro estadoRegistro);
    Optional<Paciente> findByIdAndEstadoRegistro(Long id, EstadoRegistro estadoRegistro);
}
