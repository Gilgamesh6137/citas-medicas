package com.damian.pacientes.controller;

import com.damian.commons.controller.CrudController;
import com.damian.commons.dto.pacientes.PacienteRequest;
import com.damian.commons.dto.pacientes.PacienteResponse;
import com.damian.pacientes.service.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "API Pacientes", description = "Métodos para la gestion de pacientes")
public class PacienteController extends CrudController<PacienteRequest, PacienteResponse, PacienteService> {

    public PacienteController(PacienteService service) {
        super(service);
    }

    @GetMapping("pacientes-eliminados/")
    @Operation(summary = "Listar pacientes eliminados")
    @ApiResponse(responseCode = "200", description = "Listado obtenido")
    public ResponseEntity<List<PacienteResponse>> listarEliminados() {
        return ResponseEntity.ok(service.listarEliminados());
    }
}
