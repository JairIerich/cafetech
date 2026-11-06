package com.cafetech.api.domain.manutencao;

import java.time.LocalDate;

public record DadosListagemManutencao(Long id, Long maquinaId, String modeloMaquina, TipoManutencao tipo,
                                      StatusManutencao status, LocalDate dataAbertura) {

    public DadosListagemManutencao(Manutencao manutencao) {
        this(manutencao.getId(), manutencao.getMaquina().getId(), manutencao.getMaquina().getModelo(),
                manutencao.getTipo(), manutencao.getStatus(), manutencao.getDataAbertura());
    }
}
