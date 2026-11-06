package com.cafetech.api.domain.maquina;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DadosCadastroMaquina(
        @NotBlank
        @Size(max = 80)
        String modelo,

        @NotBlank
        @Size(max = 40)
        String numeroSerie,

        LocalDate dataAquisicao,

        StatusMaquina status,

        @NotNull
        Long clienteId) {
}
