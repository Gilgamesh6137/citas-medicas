package com.damian.pacientes.service;

import com.damian.commons.clients.CitaClient;
import com.damian.commons.dto.pacientes.PacienteRequest;
import com.damian.commons.dto.pacientes.PacienteResponse;
import com.damian.commons.enums.EstadoRegistro;
import com.damian.commons.exceptions.ConflictoException;
import com.damian.commons.exceptions.RecursoNoEncontradoException;
import com.damian.pacientes.entity.Paciente;
import com.damian.pacientes.mapper.PacienteMapper;
import com.damian.pacientes.repository.PacienteRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PacienteServiceImpl implements PacienteService {

    private final PacienteRepository pacienteRepository;
    private final PacienteMapper pacienteMapper;
    private final CitaClient citaClient;

    @Override
    @Transactional(readOnly = true)
    public List<PacienteResponse> listar() {
        log.info("Listando todos los pacientes activos");

        return pacienteRepository.findByEstadoRegistro(EstadoRegistro.ACTIVO).stream()
                .map(pacienteMapper::entidadAResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteResponse obtenerPorId(Long id) {
        return pacienteMapper.entidadAResponse(obtenerPaciente(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteResponse obtenerPacientePorIdSinEstado(Long id) {
        log.info("Buscando paciente sin estado con id {}", id);

        return pacienteMapper.entidadAResponse(pacienteRepository.findById(id).orElseThrow(
                () -> new RecursoNoEncontradoException("Paciente sin estado no encontrado con id: " + id)));
    }

    @Override
    public PacienteResponse registrar(PacienteRequest request) {
        validarDatosUnicos(request);
        log.info("Registrando nuevo paciente: {}", request.nombre());

        Paciente paciente = pacienteMapper.requestAEntidad(request);
        pacienteRepository.saveAndFlush(paciente);

        log.info("Nuevo paciente: {} registrado correctamente", paciente.getNombre());
        return pacienteMapper.entidadAResponse(paciente);
    }

    @Override
    public PacienteResponse actualizar(PacienteRequest request, Long id) {
        validarCambiosUnicos(request, id);
        validarPacienteActualizarOEliminar(id);
        Paciente paciente = obtenerPaciente(id);
        log.info("Actualizando paciente con id {}", id);

        paciente.actualizar(
                request.nombre(),
                request.apellidoPaterno(),
                request.apellidoMaterno(),
                request.edad(),
                request.peso(),
                request.estatura(),
                request.email(),
                request.telefono(),
                request.direccion()
        );
        pacienteRepository.saveAndFlush(paciente);

        log.info("Paciente con id {} actualizado correctamente", id);
        return pacienteMapper.entidadAResponse(paciente);
    }

    @Override
    public void eliminar(Long id) {
        validarPacienteActualizarOEliminar(id);
        Paciente paciente = obtenerPaciente(id);
        log.info("Eliminando paciente con id {}", id);

        paciente.eliminar();
        pacienteRepository.saveAndFlush(paciente);
        log.info("Paciente con id {} eliminado correctamente", id);
    }

    private Paciente obtenerPaciente(Long id){
        log.info("Buscando paciente activo con id: {}", id);

        return pacienteRepository.findByIdAndEstadoRegistro(id, EstadoRegistro.ACTIVO).orElseThrow(() ->
                new RecursoNoEncontradoException("Paciente no encontrado con id: " + id));
    }

    private void validarDatosUnicos(PacienteRequest request){
        log.info("Validando email y teléfono únicos...");

        if (pacienteRepository.existsByEmailIgnoreCaseAndEstadoRegistro(request.email().trim(), EstadoRegistro.ACTIVO))
            throw new ConflictoException("El email: " + request.email() + " ya está registrado");

        if (pacienteRepository.existsByTelefonoAndEstadoRegistro(request.telefono().trim(), EstadoRegistro.ACTIVO))
            throw new ConflictoException("El teléfono: " + request.telefono() + " ya está registrado");
    }

    private void validarCambiosUnicos(PacienteRequest request, Long id){
        log.info("Validando cambios en el email y teléfono únicos...");

        if (pacienteRepository.existsByEmailIgnoreCaseAndEstadoRegistroAndIdNot(request.email(), EstadoRegistro.ACTIVO, id))
            throw new ConflictoException("El email: " + request.email() + " ya está registrado");

        if (pacienteRepository.existsByTelefonoAndEstadoRegistroAndIdNot(request.telefono(), EstadoRegistro.ACTIVO, id))
            throw new ConflictoException("El teléfono: " + request.telefono() + " ya está registrado");
    }

    private void validarPacienteActualizarOEliminar(Long id){
        try {
            citaClient.validarPacienteNoTieneCitasActivas(id);
        } catch (FeignException.Conflict e){
            throw new ConflictoException("El paciente con id: " + id + " tiene citas activas y no puede ser eliminado o actualizado");
        }
    }
}
