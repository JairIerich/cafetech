package com.cafetech.api.domain.cliente;

import java.time.LocalDate;

public record DadosDetalhamentoCliente(Long id, String nome, String email, String telefone,
                                       String endereco, LocalDate dataCadastro) {

    public DadosDetalhamentoCliente(Cliente cliente) {
        this(cliente.getId(), cliente.getNome(), cliente.getEmail(), cliente.getTelefone(),
                cliente.getEndereco(), cliente.getDataCadastro());
    }
}
