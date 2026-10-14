package com.damian.citas.service;

import com.damian.citas.dto.CitaRequest;
import com.damian.citas.dto.CitaResponse;
import com.damian.citas.entity.Cita;
import com.damian.citas.enums.EstadoCita;
import com.damian.citas.mapper.CitaMapper;
import com.damian.citas.repository.CitaRepository;
import com.damian.commons.clients.MedicoClient;
import com.damian.commons.clients.PacienteClient;
import com.damian.commons.dto.medicos.MedicoResponse;
import com.damian.commons.dto.pacientes.PacienteResponse;
import com.damian.commons.enums.DisponibilidadMedico;
import com.damian.commons.enums.EstadoRegistro;
import com.damian.commons.exceptions.ConflictoException;
import com.damian.commons.exceptions.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CitaServiceImpl implements CitaService{

    private final CitaRepository citaRepository;
    private final CitaMapper citaMapper;

    private final MedicoClient medicoClient;
    private final PacienteClient pacienteClient;

    private final List<EstadoCita> estadoCitas = List.of(EstadoCita.PENDIENTE, EstadoCita.CONFIRMADA, EstadoCita.EN_CURSO);

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponse> listar() {
        log.info("Listando todas las citas activas");
        return citaRepository.findByEstadoRegistro(EstadoRegistro.ACTIVO).stream()
                .map(cita -> citaMapper.entidadAResponse(cita,
                        obtenerPacienteSinEstado(cita.getIdPaciente()),
                        obtenerMedicoSinEstado(cita.getIdMedico()))
                ).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CitaResponse obtenerPorId(Long id) {
        Cita cita = obtenerCitaOException(id);
        return citaMapper.entidadAResponse(
                cita, obtenerPacienteSinEstado(cita.getIdPaciente()), obtenerMedicoSinEstado(cita.getIdMedico()));
    }

    @Override
    public CitaResponse registrar(CitaRequest request) {
        log.info("Registrando nueva cita...");

        MedicoResponse medico = obtenerMedicoActivo(request.idMedico());
        validarMedicoActivoDisponible(medico);

        PacienteResponse paciente = obtenerPacienteActivo(request.idPaciente());
        validarPacienteNoTieneCitasActivas(paciente.id());

        Cita cita = citaMapper.requestAEntidad(request);
        citaRepository.saveAndFlush(cita);

        log.info("Cita registrada exitosamente");
        cambiarDisponibilidadMedicoSegunEstadoCita(medico.id(), cita.getEstadoCita());
        return citaMapper.entidadAResponse(cita, paciente, medico);
    }

    @Override
    public CitaResponse actualizar(CitaRequest request, Long id) {
        Cita cita = obtenerCitaOException(id);

        if (cita.getEstadoCita() == EstadoCita.EN_CURSO)
            throw new ConflictoException("La cita está en curso y no puede actualizarse");

        Long idMedicoAntiguo = cita.getIdMedico();
        Long idPacienteAntiguo = cita.getIdPaciente();
        boolean cambioDeMedico = !idMedicoAntiguo.equals(request.idMedico());

        if (cambioDeMedico) {
            MedicoResponse nuevoMedico = obtenerMedicoActivo(request.idMedico());
            validarMedicoActivoDisponible(nuevoMedico);
        }

        if (!idPacienteAntiguo.equals(request.idPaciente())) {
            validarPacienteNoTieneCitasActivas(request.idPaciente());
        }

        log.info("Actualizando cita con id: {}", id);
        cita.actualizar(request.idPaciente(), request.idMedico(), request.fechaCita(), request.sintomas());
        citaRepository.saveAndFlush(cita);
        log. info("Cita actualizada con id: {}", id);

        if (cambioDeMedico){
            actualizarDisponibilidadMedico(idMedicoAntiguo, DisponibilidadMedico.DISPONIBLE.getCodigo());
            actualizarDisponibilidadMedico(request.idMedico(), DisponibilidadMedico.NO_DISPONIBLE.getCodigo());
        }

        MedicoResponse medicoFinal = obtenerMedicoActivo(cita.getIdMedico());
        PacienteResponse pacienteFinal = obtenerPacienteActivo(cita.getIdPaciente());
        return citaMapper.entidadAResponse(cita, pacienteFinal, medicoFinal);
    }

    @Override
    public void actualizarEstadoCita(Long idCita, Long idEstadoCita) {
        Cita cita = obtenerCitaOException(idCita);
        log.info("Actualizando estado de la cita con id: {}", idCita);

        cita.actualizarEstadoCita(EstadoCita.obtenerEstadoCitaPorCodigo(idEstadoCita));
        citaRepository.saveAndFlush(cita);

        cambiarDisponibilidadMedicoSegunEstadoCita(cita.getIdMedico(), cita.getEstadoCita());
        log.info("Estado de la cita {} actualizando correctamente", idCita);
    }

    @Override
    public void eliminar(Long id) {
        Cita cita = obtenerCitaOException(id);
        log.info("Eliminando cita con id: {}", id);

        cita.eliminar();
        citaRepository.saveAndFlush(cita);

        if (cita.getEstadoCita() == EstadoCita.PENDIENTE)
            actualizarDisponibilidadMedico(cita.getIdMedico(), DisponibilidadMedico.DISPONIBLE.getCodigo());

        log.info("Cita con id {} ha sido marcada como eliminada", id);
    }

    @Override
    public void validarMedicoNoTieneCitasActivas(Long id) {
        log.info("Validando que el medico con id: {} no tenga citas activas", id);

        if (citaRepository.existsByIdMedicoAndEstadoRegistroAndEstadoCitaIn(id, EstadoRegistro.ACTIVO, estadoCitas))
            throw new ConflictoException("El medico con id: " + id + " ya tiene una cita activa");
    }

    @Override
    public void validarPacienteNoTieneCitasActivas(Long id){
        log.info("Validando que el paciente con id: {} no tenga citas activas", id);

        if (citaRepository.existsByIdPacienteAndEstadoRegistroAndEstadoCitaIn(id, EstadoRegistro.ACTIVO, estadoCitas))
            throw new ConflictoException("El paciente con id: " + id + " ya tiene una cita activa");
    }

    //=== Métodos Citas ===
    private Cita obtenerCitaOException(Long id){
        log.info("Buscando cita con id: {}", id);
        return citaRepository.findById(id).orElseThrow(() ->
                new RecursoNoEncontradoException("Cita no encontrada con id: " + id));
    }

    //=== Métodos Medicos ===
    private MedicoResponse obtenerMedicoActivo(Long id){
        log.info("Buscando médico activo con id {} en el servicio remoto...", id);
        return medicoClient.obtenerMedicoActivoPorId(id);
    }

    private MedicoResponse obtenerMedicoSinEstado(Long id){
        log.info("Buscando médico sin estado con id {} en el servicio remoto...", id);
        return medicoClient.obtenerMedicoSinEstadoPorId(id);
    }

    private void validarMedicoActivoDisponible(MedicoResponse medico){
        log.info("Validando si el médico activo está disponible...");
        if (!DisponibilidadMedico.DISPONIBLE.getCodigo().equals(medico.idDisponibilidad()))
            throw new ConflictoException("El médico activo no está disponible para consulta");
    }

    private void actualizarDisponibilidadMedico(Long idMedico, Long idDisponibilidad){
        log.info("Actualizando disponibilidad del médico en el servicio remoto");
        medicoClient.actualizarDisponibilidadMedico(idMedico, idDisponibilidad);
        log.info("Disponibilidad del médico actualizado en el servicio remoto");
    }

    private void cambiarDisponibilidadMedicoSegunEstadoCita(Long idMedico, EstadoCita estadoCita){
        switch (estadoCita){
            case PENDIENTE, CONFIRMADA ->
                    actualizarDisponibilidadMedico(idMedico, DisponibilidadMedico.NO_DISPONIBLE.getCodigo());
            case EN_CURSO ->
                    actualizarDisponibilidadMedico(idMedico, DisponibilidadMedico.EN_CONSULTA.getCodigo());
            case FINALIZADA, CANCELADA ->
                    actualizarDisponibilidadMedico(idMedico, DisponibilidadMedico.DISPONIBLE.getCodigo());
        }
    }

    //=== Métodos Pacientes ===
    private PacienteResponse obtenerPacienteActivo(Long id){
        log.info("Buscando paciente activo con id {} en el servicio remoto...", id);
        return pacienteClient.obtenerPacienteActivoPorId(id);
    }

    private PacienteResponse obtenerPacienteSinEstado(Long id){
        log.info("Buscando paciente sin estado con id {} en el servicio remoto...", id);
        return pacienteClient.obtenerPacienteSinEstadoPorId(id);
    }
}
