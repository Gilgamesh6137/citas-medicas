package com.damian.commons.dto.medicos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de un médico asociado a una cita")
public record DatosMedico(

        @Schema(name = "Nombre completo del médico", example = "Pedro Damián Vásquez")
        String nombre,

        @Schema(name = "Cédula profesional del médico", example = "123456789012")
        String cedulaProfesional,

        @Schema(name = "Nombre de la especialidad del médico", example = "Cardiología")
        String especialidad
) {}
