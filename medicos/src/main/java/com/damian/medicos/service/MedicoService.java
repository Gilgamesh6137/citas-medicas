package com.damian.medicos.service;

import com.damian.commons.dto.medicos.MedicoRequest;
import com.damian.commons.dto.medicos.MedicoResponse;
import com.damian.commons.service.CrudService;

public interface MedicoService extends CrudService<MedicoRequest, MedicoResponse> {

    MedicoResponse obtenerMedicoPorIdSinEstado(Long id);
    void actualizarDisponibilidadMedico(Long idMedico, Long idDisponibilidad);
}
