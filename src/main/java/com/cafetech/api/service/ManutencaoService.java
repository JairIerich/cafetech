package com.cafetech.api.service;

import com.cafetech.api.domain.maquina.Maquina;
import com.cafetech.api.domain.maquina.MaquinaRepository;
import com.cafetech.api.domain.maquina.StatusMaquina;
import com.cafetech.api.domain.manutencao.DadosAndamentoManutencao;
import com.cafetech.api.domain.manutencao.DadosCadastroManutencao;
import com.cafetech.api.domain.manutencao.DadosConclusaoManutencao;
import com.cafetech.api.domain.manutencao.DadosDetalhamentoManutencao;
import com.cafetech.api.domain.manutencao.DadosListagemManutencao;
import com.cafetech.api.domain.manutencao.Manutencao;
import com.cafetech.api.domain.manutencao.ManutencaoRepository;
import com.cafetech.api.domain.manutencao.StatusManutencao;
import com.cafetech.api.exception.ValidacaoException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ManutencaoService {

    @Autowired
    private ManutencaoRepository repository;

    @Autowired
    private MaquinaRepository maquinaRepository;

    @Transactional(readOnly = true)
    public Page<DadosListagemManutencao> listar(Long maquinaId, StatusManutencao status, Pageable paginacao) {
        if (maquinaId != null) {
            return repository.findAllByMaquinaId(maquinaId, paginacao).map(DadosListagemManutencao::new);
        }
        if (status != null) {
            return repository.findAllByStatus(status, paginacao).map(DadosListagemManutencao::new);
        }
        return repository.findAll(paginacao).map(DadosListagemManutencao::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoManutencao detalhar(Long id) {
        Manutencao manutencao = repository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
        return new DadosDetalhamentoManutencao(manutencao);
    }

    @Transactional
    public DadosDetalhamentoManutencao abrir(DadosCadastroManutencao dados) {
        Maquina maquina = maquinaRepository.findById(dados.maquinaId())
                .orElseThrow(EntityNotFoundException::new);
        if (maquina.getStatus() == StatusMaquina.INATIVA) {
            throw new ValidacaoException("Nao e possivel abrir manutencao para uma maquina inativa!");
        }
        if (repository.existsByMaquinaIdAndStatusNot(maquina.getId(), StatusManutencao.CONCLUIDA)) {
            throw new ValidacaoException("Maquina ja possui manutencao em aberto!");
        }
        Manutencao manutencao = new Manutencao(dados, maquina);
        repository.save(manutencao);
        maquina.setStatus(StatusMaquina.EM_MANUTENCAO);
        return new DadosDetalhamentoManutencao(manutencao);
    }

    @Transactional
    public DadosDetalhamentoManutencao iniciarEmAndamento(DadosAndamentoManutencao dados) {
        Manutencao manutencao = repository.findById(dados.id())
                .orElseThrow(EntityNotFoundException::new);
        if (manutencao.getStatus() != StatusManutencao.ABERTA) {
            throw new ValidacaoException("Somente manutencao ABERTA pode ir para EM_ANDAMENTO!");
        }
        manutencao.iniciar();
        return new DadosDetalhamentoManutencao(manutencao);
    }

    @Transactional
    public DadosDetalhamentoManutencao concluir(DadosConclusaoManutencao dados) {
        Manutencao manutencao = repository.findById(dados.id())
                .orElseThrow(EntityNotFoundException::new);
        if (manutencao.getStatus() == StatusManutencao.CONCLUIDA) {
            throw new ValidacaoException("Manutencao ja esta CONCLUIDA!");
        }
        LocalDate dataConclusao = dados.dataConclusao() == null ? LocalDate.now() : dados.dataConclusao();
        if (dataConclusao.isBefore(manutencao.getDataAbertura())) {
            throw new ValidacaoException("Data de conclusao nao pode ser anterior a data de abertura!");
        }
        manutencao.concluir(dataConclusao);
        Maquina maquina = manutencao.getMaquina();
        if (!repository.existsByMaquinaIdAndStatusNot(maquina.getId(), StatusManutencao.CONCLUIDA)
                && maquina.getStatus() == StatusMaquina.EM_MANUTENCAO) {
            maquina.setStatus(StatusMaquina.ATIVA);
        }
        return new DadosDetalhamentoManutencao(manutencao);
    }
}
