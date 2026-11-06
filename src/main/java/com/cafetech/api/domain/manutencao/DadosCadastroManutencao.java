package com.cafetech.api.domain.manutencao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DadosCadastroManutencao(
        @NotNull
        Long maquinaId,

        @NotBlank
        @Size(max = 500)
        String descricao,

        @NotNull
        TipoManutencao tipo,

        @Size(max = 80)
        String tecnico,

        LocalDate dataAbertura) {
}
