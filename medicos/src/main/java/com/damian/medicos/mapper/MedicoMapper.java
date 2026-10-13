package com.damian.medicos.mapper;

import com.damian.commons.dto.medicos.MedicoRequest;
import com.damian.commons.dto.medicos.MedicoResponse;
import com.damian.commons.enums.EspecialidadMedico;
import com.damian.commons.mapper.CommonMapper;
import com.damian.medicos.entity.Medico;
import org.springframework.stereotype.Component;

@Component
public class MedicoMapper implements CommonMapper<MedicoRequest, MedicoResponse, Medico> {

    @Override
    public Medico requestAEntidad(MedicoRequest request) {
        return request == null ? null : Medico.crear(
                request.nombre(),
                request.apellidoPaterno(),
                request.apellidoMaterno(),
                request.edad(),
                request.email(),
                request.telefono(),
                request.cedulaProfesional()
        );
    }

    public Medico requestAEntidad(MedicoRequest request, EspecialidadMedico especialidadMedico){
        Medico medico = requestAEntidad(request);
        medico.actualizarEspecialidad(especialidadMedico);
        return medico;
    }

    @Override
    public MedicoResponse entidadAResponse(Medico entidad) {
        return entidad == null ? null : new MedicoResponse(
                entidad.getId(),
                String.join(" ",
                        entidad.getNombre(),
                        entidad.getApellidoPaterno(),
                        entidad.getApellidoMaterno()),
                entidad.getEdad(),
                entidad.getEmail(),
                entidad.getTelefono(),
                entidad.getCedulaProfesional(),
                entidad.getEspecialidad().getDescripcion(),
                entidad.getDisponibilidad().getDescripcion(),
                entidad.getDisponibilidad().getCodigo()
        );
    }
}
