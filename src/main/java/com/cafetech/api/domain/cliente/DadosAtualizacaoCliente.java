package com.cafetech.api.domain.cliente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DadosAtualizacaoCliente(
        @NotNull
        Long id,

        @Size(max = 120)
        String nome,

        @Email
        @Size(max = 120)
        String email,

        @Size(max = 20)
        String telefone,

        @Size(max = 200)
        String endereco) {
}
