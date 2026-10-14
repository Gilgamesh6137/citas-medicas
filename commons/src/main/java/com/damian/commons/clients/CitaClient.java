package com.damian.commons.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "citas")
public interface CitaClient {

    @GetMapping("/medico-cita/{idMedico}")
    ResponseEntity<Void> validarMedicoNoTieneCitasActivas(@PathVariable Long idMedico);

    @GetMapping("/paciente-cita/{idPaciente}")
    ResponseEntity<Void> validarPacienteNoTieneCitasActivas(@PathVariable Long idPaciente);
}
