package com.damian.medicos.service;

import com.damian.commons.clients.CitaClient;
import com.damian.commons.dto.medicos.MedicoRequest;
import com.damian.commons.dto.medicos.MedicoResponse;
import com.damian.commons.enums.DisponibilidadMedico;
import com.damian.commons.enums.EspecialidadMedico;
import com.damian.commons.enums.EstadoRegistro;
import com.damian.commons.exceptions.ConflictoException;
import com.damian.commons.exceptions.DatoInvalidoException;
import com.damian.commons.exceptions.RecursoNoEncontradoException;
import com.damian.medicos.entity.Medico;
import com.damian.medicos.mapper.MedicoMapper;
import com.damian.medicos.repository.MedicoRepository;
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
public class MedicoServiceImpl implements MedicoService{

    private final MedicoRepository medicoRepository;
    private final MedicoMapper medicoMapper;
    private final CitaClient citaClient;

    @Override
    @Transactional(readOnly = true)
    public List<MedicoResponse> listar() {
        log.info("Listando todos los médicos activos");

        return medicoRepository.findByEstadoRegistro(EstadoRegistro.ACTIVO).stream()
                .map(medicoMapper::entidadAResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MedicoResponse obtenerPorId(Long id) {
        return medicoMapper.entidadAResponse(obtenerMedicoActivoPorId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public MedicoResponse obtenerMedicoPorIdSinEstado(Long id) {
        log.info("Buscando médico sin estado con id {}", id);

        return medicoMapper.entidadAResponse(medicoRepository.findById(id).orElseThrow(
                () -> new RecursoNoEncontradoException("Médico sin estado no encontrado con id: " + id)));
    }

    @Override
    public MedicoResponse registrar(MedicoRequest request) {
        log.info("Registrando nuevo médico: {}", request.nombre());

        validarDatosUnicos(request);
        Medico medico = medicoMapper.requestAEntidad(request, EspecialidadMedico.obtenerEspecialidadPorCodigo(request.idEspecialidad()));
        medicoRepository.saveAndFlush(medico);

        log.info("Nuevo médico registrado: {}", medico.getNombre());
        return medicoMapper.entidadAResponse(medico);
    }

    @Override
    public MedicoResponse actualizar(MedicoRequest request, Long id) {
        Medico medico = obtenerMedicoActivoPorId(id);
        validarMedicoActualizarOEliminar(id);
        validarCambiosUnicos(request, id);
        log.info("Actualizando médico con id: {}", id);

        medico.actualizar(
                request.nombre(),
                request.apellidoPaterno(),
                request.apellidoMaterno(),
                request.edad(),
                request.email(),
                request.telefono(),
                request.cedulaProfesional(),
                EspecialidadMedico.obtenerEspecialidadPorCodigo(request.idEspecialidad()));
        medicoRepository.saveAndFlush(medico);

        log.info("Médico con id {} actualizado correctamente", id);
        return medicoMapper.entidadAResponse(medico);
    }

    @Override
    public void actualizarDisponibilidadMedico(Long idMedico, Long idDisponibilidad) {
        Medico medico = obtenerMedicoActivoPorId(idMedico);
        log.info("Actualizando disponibilidad del médico con id: {}", idMedico);

        DisponibilidadMedico nuevaDisponibilidad = DisponibilidadMedico.obtenerDisponibilidadPorCodigo(idDisponibilidad);
        DisponibilidadMedico disponibilidadAnterior = medico.getDisponibilidad();
        medico.actualizarDisponibilidad(nuevaDisponibilidad);

        medicoRepository.saveAndFlush(medico);
        log.info("Disponibilidad del médico con id {} cambió de {} a {}", idMedico, disponibilidadAnterior, nuevaDisponibilidad);
    }

    @Override
    public void eliminar(Long id) {
        Medico medico = obtenerMedicoActivoPorId(id);
        validarMedicoActualizarOEliminar(id);

        log.info("Crucificando al médico con id: {}", id);
        medico.eliminar();
        medicoRepository.saveAndFlush(medico);
        log.info("Médico ha sido crucificado exitósamente");
    }

    private Medico obtenerMedicoActivoPorId(Long id) {
        log.info("Buscando médico activo con id {}", id);

        return medicoRepository.findByIdAndEstadoRegistro(id, EstadoRegistro.ACTIVO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Médico activo no encontrado con id: " + id));
    }

    private void validarDatosUnicos(MedicoRequest request) {
        log.info("Validando email único...");

        if (medicoRepository.existsByEmailIgnoreCaseAndEstadoRegistro(request.email(), EstadoRegistro.ACTIVO))
            throw new DatoInvalidoException("Ya existe un médico activo registrado con el email: " + request.email());

        log.info("Validando teléfono único...");

        if (medicoRepository.existsByTelefonoAndEstadoRegistro(request.telefono(), EstadoRegistro.ACTIVO))
            throw new DatoInvalidoException("Ya existe un médico activo registrado con el teléfono: " + request.telefono());

        log.info("Validando cédula profesional única...");

        if (medicoRepository.existsByCedulaProfesionalIgnoreCaseAndEstadoRegistro(request.cedulaProfesional(), EstadoRegistro.ACTIVO))
            throw new DatoInvalidoException("Ya existe un médico activo registrado con la cédula profesional: " + request.cedulaProfesional());
    }

    private void validarCambiosUnicos(MedicoRequest request, Long id) {
        log.info("Validando cambio en email único...");

        if (medicoRepository.existsByEmailIgnoreCaseAndEstadoRegistroAndIdNot(request.email(), EstadoRegistro.ACTIVO, id))
            throw new DatoInvalidoException("Ya existe un médico activo registrado con el email: " + request.email());

        log.info("Validando cambio en teléfono único...");

        if (medicoRepository.existsByTelefonoAndEstadoRegistroAndIdNot(request.telefono(), EstadoRegistro.ACTIVO, id))
            throw new DatoInvalidoException("Ya existe un médico activo registrado con el teléfono: " + request.telefono());

        log.info("Validando cambio en cédula profesional única...");

        if (medicoRepository.existsByCedulaProfesionalIgnoreCaseAndEstadoRegistroAndIdNot(request.cedulaProfesional(), EstadoRegistro.ACTIVO, id))
            throw new DatoInvalidoException("Ya existe un médico activo registrado con la cédula profesional: " + request.cedulaProfesional());
    }

    private void validarMedicoActualizarOEliminar(Long id){
        try {
            citaClient.validarMedicoNoTieneCitasActivas(id);
        } catch (FeignException.Conflict e){
            throw new ConflictoException("El medico con id: " + id + " tiene citas activas y no puede ser eliminado o actualizado");
        }
    }
}
