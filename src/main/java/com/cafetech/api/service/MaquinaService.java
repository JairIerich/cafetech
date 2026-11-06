package com.cafetech.api.service;

import com.cafetech.api.domain.cliente.Cliente;
import com.cafetech.api.domain.cliente.ClienteRepository;
import com.cafetech.api.domain.maquina.DadosAtualizacaoMaquina;
import com.cafetech.api.domain.maquina.DadosCadastroMaquina;
import com.cafetech.api.domain.maquina.DadosDetalhamentoMaquina;
import com.cafetech.api.domain.maquina.DadosListagemMaquina;
import com.cafetech.api.domain.maquina.Maquina;
import com.cafetech.api.domain.maquina.MaquinaRepository;
import com.cafetech.api.domain.maquina.StatusMaquina;
import com.cafetech.api.domain.manutencao.ManutencaoRepository;
import com.cafetech.api.domain.manutencao.StatusManutencao;
import com.cafetech.api.exception.ValidacaoException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaquinaService {

    @Autowired
    private MaquinaRepository repository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ManutencaoRepository manutencaoRepository;

    @Transactional(readOnly = true)
    public Page<DadosListagemMaquina> listar(Long clienteId, StatusMaquina status, Pageable paginacao) {
        if (clienteId != null && status != null) {
            return repository.findAllByClienteIdAndStatus(clienteId, status, paginacao).map(DadosListagemMaquina::new);
        }
        if (clienteId != null) {
            return repository.findAllByClienteId(clienteId, paginacao).map(DadosListagemMaquina::new);
        }
        if (status != null) {
            return repository.findAllByStatus(status, paginacao).map(DadosListagemMaquina::new);
        }
        return repository.findAll(paginacao).map(DadosListagemMaquina::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoMaquina detalhar(Long id) {
        Maquina maquina = repository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
        return new DadosDetalhamentoMaquina(maquina);
    }

    @Transactional
    public DadosDetalhamentoMaquina cadastrar(DadosCadastroMaquina dados) {
        Cliente cliente = clienteRepository.findById(dados.clienteId())
                .orElseThrow(EntityNotFoundException::new);
        if (repository.existsByNumeroSerieIgnoreCase(dados.numeroSerie())) {
            throw new ValidacaoException("Numero de serie ja cadastrado para outra maquina!");
        }
        Maquina maquina = new Maquina(dados, cliente);
        repository.save(maquina);
        return new DadosDetalhamentoMaquina(maquina);
    }

    @Transactional
    public DadosDetalhamentoMaquina atualizar(DadosAtualizacaoMaquina dados) {
        Maquina maquina = repository.findById(dados.id())
                .orElseThrow(EntityNotFoundException::new);
        Cliente cliente = null;
        if (dados.clienteId() != null) {
            cliente = clienteRepository.findById(dados.clienteId())
                    .orElseThrow(EntityNotFoundException::new);
        }
        if (dados.numeroSerie() != null) {
            repository.findByNumeroSerieIgnoreCase(dados.numeroSerie())
                    .filter(outra -> maquina.getId() == null || !outra.getId().equals(maquina.getId()))
                    .ifPresent(outra -> {
                        throw new ValidacaoException("Numero de serie ja cadastrado para outra maquina!");
                    });
        }
        maquina.atualizarInformacoes(dados, cliente);
        return new DadosDetalhamentoMaquina(maquina);
    }

    @Transactional
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException();
        }
        if (manutencaoRepository.existsByMaquinaIdAndStatusNot(id, StatusManutencao.CONCLUIDA)) {
            throw new ValidacaoException("Maquina nao pode ser excluida pois possui manutencao em aberto!");
        }
        manutencaoRepository.deleteByMaquinaId(id); // o historico de manutencoes pertence a maquina
        repository.deleteById(id);
    }
}
