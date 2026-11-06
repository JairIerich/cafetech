package com.cafetech.api.domain.maquina;

public record DadosListagemMaquina(Long id, String modelo, String numeroSerie,
                                   StatusMaquina status, Long clienteId, String nomeCliente) {

    public DadosListagemMaquina(Maquina maquina) {
        this(maquina.getId(), maquina.getModelo(), maquina.getNumeroSerie(), maquina.getStatus(),
                maquina.getCliente().getId(), maquina.getCliente().getNome());
    }
}
