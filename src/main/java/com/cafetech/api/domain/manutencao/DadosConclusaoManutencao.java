package com.cafetech.api.domain.manutencao;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DadosConclusaoManutencao(@NotNull Long id, LocalDate dataConclusao) {
}
