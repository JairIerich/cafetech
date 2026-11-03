package com.cafetech.api.domain.cliente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosCadastroCliente(
        @NotBlank
        @Size(max = 120)
        String nome,

        @NotBlank
        @Email
        @Size(max = 120)
        String email,

        @Size(max = 20)
        String telefone,

        @Size(max = 200)
        String endereco) {
}
