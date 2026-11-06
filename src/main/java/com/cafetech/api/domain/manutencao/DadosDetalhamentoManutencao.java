package com.cafetech.api.domain.manutencao;

import java.time.LocalDate;

public record DadosDetalhamentoManutencao(Long id, Long maquinaId, String modeloMaquina, String descricao,
                                          TipoManutencao tipo, StatusManutencao status,
                                          LocalDate dataAbertura, LocalDate dataConclusao, String tecnico) {

    public DadosDetalhamentoManutencao(Manutencao manutencao) {
        this(manutencao.getId(), manutencao.getMaquina().getId(), manutencao.getMaquina().getModelo(),
                manutencao.getDescricao(), manutencao.getTipo(), manutencao.getStatus(),
                manutencao.getDataAbertura(), manutencao.getDataConclusao(), manutencao.getTecnico());
    }
}
