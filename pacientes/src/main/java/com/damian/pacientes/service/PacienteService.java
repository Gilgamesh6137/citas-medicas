package com.damian.pacientes.service;

import com.damian.commons.dto.pacientes.PacienteRequest;
import com.damian.commons.dto.pacientes.PacienteResponse;
import com.damian.commons.service.CrudService;

public interface PacienteService extends CrudService<PacienteRequest, PacienteResponse> {

    PacienteResponse obtenerPacientePorIdSinEstado(Long id);
}
