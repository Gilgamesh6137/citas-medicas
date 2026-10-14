package com.damian.citas.controller;

import com.damian.citas.dto.CitaRequest;
import com.damian.citas.dto.CitaResponse;
import com.damian.citas.service.CitaService;
import com.damian.commons.controller.CrudController;
import com.damian.commons.docs.ProblemaDoc;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "API Citas", description = "Métodos par la gestión de citas")
public class CitasController extends CrudController<CitaRequest, CitaResponse, CitaService> {

    public CitasController(CitaService service){
        super(service);
    }

    @PatchMapping("/{idCita}/estado/{idEstado}")
    @Operation(summary = "Actualizar estado de la cita",
    description = "Actualiza el estado de una cita utilizando el identificador de la cita y el identificador del nuevo estado")
    @ApiResponse(responseCode = "204", description = "Estado de la cita actualizado")
    @ApiResponse(responseCode = "409", description = "El estado de la cita no puede actualizarse por regla de negocio",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    public ResponseEntity<Void> actualizarEstadoCita(
            @PathVariable @Positive(message = "El idCita debe ser positivo") Long idCita,
            @PathVariable @Positive(message = "El idEstado debe ser positivo") Long idEstado){
        service.actualizarEstadoCita(idCita, idEstado);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/medico-cita/{idMedico}")
    @ApiResponse(responseCode = "204", description = "El medico no tiene citas activas")
    @ApiResponse(responseCode = "409", description = "El medico tiene una cita activa y no puede agendar otra",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    public ResponseEntity<Void> medicoTieneCitaActiva(
            @PathVariable @Positive(message = "El idMedico debe ser positivo") Long idMedico
    ) {
        service.validarMedicoNoTieneCitasActivas(idMedico);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/paciente-cita/{idPaciente}")
    @ApiResponse(responseCode = "204", description = "El paciente no tiene citas activas")
    @ApiResponse(responseCode = "409", description = "El paciente tiene una cita activa y no puede agendar otra",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    public ResponseEntity<Void> pacienteTieneCitaActiva(
            @PathVariable @Positive(message = "El idPaciente debe ser positivo") Long idPaciente
    ) {
        service.validarPacienteNoTieneCitasActivas(idPaciente);
        return ResponseEntity.noContent().build();
    }
}
