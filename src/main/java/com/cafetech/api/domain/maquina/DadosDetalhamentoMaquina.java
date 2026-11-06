package com.cafetech.api.domain.maquina;

import java.time.LocalDate;

public record DadosDetalhamentoMaquina(Long id, String modelo, String numeroSerie, LocalDate dataAquisicao,
                                       StatusMaquina status, Long clienteId, String nomeCliente) {

    public DadosDetalhamentoMaquina(Maquina maquina) {
        this(maquina.getId(), maquina.getModelo(), maquina.getNumeroSerie(), maquina.getDataAquisicao(),
                maquina.getStatus(), maquina.getCliente().getId(), maquina.getCliente().getNome());
    }
}
