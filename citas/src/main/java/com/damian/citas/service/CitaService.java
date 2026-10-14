package com.damian.citas.service;

import com.damian.citas.dto.CitaRequest;
import com.damian.citas.dto.CitaResponse;
import com.damian.commons.service.CrudService;

public interface CitaService extends CrudService<CitaRequest, CitaResponse> {

    void actualizarEstadoCita(Long idCita, Long idEstadoCita);
    void validarMedicoNoTieneCitasActivas(Long idMedico);
    void validarPacienteNoTieneCitasActivas(Long idPaciente);
}
