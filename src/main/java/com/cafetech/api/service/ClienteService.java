package com.cafetech.api.service;

import com.cafetech.api.domain.cliente.Cliente;
import com.cafetech.api.domain.cliente.ClienteRepository;
import com.cafetech.api.domain.cliente.DadosAtualizacaoCliente;
import com.cafetech.api.domain.cliente.DadosCadastroCliente;
import com.cafetech.api.domain.cliente.DadosDetalhamentoCliente;
import com.cafetech.api.domain.cliente.DadosListagemCliente;
import com.cafetech.api.domain.maquina.MaquinaRepository;
import com.cafetech.api.exception.ValidacaoException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository repository;

    @Autowired
    private MaquinaRepository maquinaRepository;

    public Page<DadosListagemCliente> listar(Pageable paginacao) {
        return repository.findAll(paginacao).map(DadosListagemCliente::new);
    }

    public DadosDetalhamentoCliente detalhar(Long id) {
        Cliente cliente = repository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
        return new DadosDetalhamentoCliente(cliente);
    }

    @Transactional
    public DadosDetalhamentoCliente cadastrar(DadosCadastroCliente dados) {
        if (repository.existsByEmailIgnoreCase(dados.email())) {
            throw new ValidacaoException("E-mail ja cadastrado para outro cliente!");
        }
        Cliente cliente = new Cliente(dados);
        repository.save(cliente);
        return new DadosDetalhamentoCliente(cliente);
    }

    @Transactional
    public DadosDetalhamentoCliente atualizar(DadosAtualizacaoCliente dados) {
        Cliente cliente = repository.findById(dados.id())
                .orElseThrow(EntityNotFoundException::new);
        if (dados.email() != null) {
            repository.findByEmailIgnoreCase(dados.email())
                    .filter(outro -> cliente.getId() == null || !outro.getId().equals(cliente.getId()))
                    .ifPresent(outro -> {
                        throw new ValidacaoException("E-mail ja cadastrado para outro cliente!");
                    });
        }
        cliente.atualizarInformacoes(dados);
        return new DadosDetalhamentoCliente(cliente);
    }

    @Transactional
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException();
        }
        if (maquinaRepository.existsByClienteId(id)) {
            throw new ValidacaoException("Cliente nao pode ser excluido pois possui maquinas cadastradas!");
        }
        repository.deleteById(id);
    }
}
