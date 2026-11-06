package com.cafetech.api.domain.maquina;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DadosAtualizacaoMaquina(
        @NotNull
        Long id,

        @Size(max = 80)
        String modelo,

        @Size(max = 40)
        String numeroSerie,

        LocalDate dataAquisicao,

        StatusMaquina status,

        Long clienteId) {
}
