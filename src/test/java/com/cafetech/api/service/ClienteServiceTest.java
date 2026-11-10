package com.cafetech.api.service;

import com.cafetech.api.domain.cliente.Cliente;
import com.cafetech.api.domain.cliente.ClienteRepository;
import com.cafetech.api.domain.cliente.DadosCadastroCliente;
import com.cafetech.api.domain.cliente.DadosAtualizacaoCliente;
import com.cafetech.api.domain.cliente.DadosDetalhamentoCliente;
import com.cafetech.api.domain.maquina.MaquinaRepository;
import com.cafetech.api.exception.ValidacaoException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @InjectMocks
    private ClienteService service;

    @Mock
    private ClienteRepository repository;

    @Mock
    private MaquinaRepository maquinaRepository;

    @Test
    void deveriaCadastrarCliente() {
        //Arrange
        var dados = new DadosCadastroCliente("Maria Silva", "maria@cafe.com", "11999990000", "Rua das Flores, 100");
        given(repository.existsByEmailIgnoreCase(dados.email())).willReturn(false);

        //Act
        var dto = service.cadastrar(dados);

        //Assert
        then(repository).should().save(new Cliente(dados));
        assertEquals("Maria Silva", dto.nome());
        assertEquals("maria@cafe.com", dto.email());
    }

    @Test
    void naoDeveriaCadastrarClienteComEmailDuplicado() {
        //Arrange
        var dados = new DadosCadastroCliente("Maria Silva", "maria@cafe.com", "11999990000", "Rua das Flores, 100");
        given(repository.existsByEmailIgnoreCase(dados.email())).willReturn(true);

        //Act
        var ex = assertThrows(ValidacaoException.class, () -> service.cadastrar(dados));

        //Assert
        then(repository).should(never()).save(any());
        assertEquals("E-mail ja cadastrado para outro cliente!", ex.getMessage());
    }

    @Test
    void deveriaDetalharCliente() {
        //Arrange
        var cliente = new Cliente(new DadosCadastroCliente("Maria Silva", "maria@cafe.com", null, null));
        given(repository.findById(1L)).willReturn(java.util.Optional.of(cliente));

        //Act
        var dto = service.detalhar(1L);

        //Assert
        assertEquals("maria@cafe.com", dto.email());
    }

    @Test
    void deveriaLancar404QuandoClienteNaoExiste() {
        //Arrange
        given(repository.findById(99L)).willReturn(java.util.Optional.empty());

        //Act //Assert
        assertThrows(EntityNotFoundException.class, () -> service.detalhar(99L));
    }

    @Test
    void deveriaAtualizarCliente() {
        //Arrange
        var cliente = new Cliente(new DadosCadastroCliente("Maria Silva", "maria@cafe.com", null, null));
        var dados = new DadosAtualizacaoCliente(1L, "Maria Souza", null, "11888880000", null);
        given(repository.findById(dados.id())).willReturn(java.util.Optional.of(cliente));

        //Act
        var dto = service.atualizar(dados);

        //Assert
        assertEquals("Maria Souza", dto.nome());
        assertEquals("11888880000", dto.telefone());
    }

    @Test
    void naoDeveriaAtualizarClienteComEmailDeOutroCliente() {
        //Arrange
        var cliente = new Cliente(new DadosCadastroCliente("Maria Silva", "maria@cafe.com", null, null));
        var outro = new Cliente(new DadosCadastroCliente("Joao", "joao@cafe.com", null, null));
        var dados = new DadosAtualizacaoCliente(1L, "Maria Silva", "joao@cafe.com", null, null);
        given(repository.findById(dados.id())).willReturn(java.util.Optional.of(cliente));
        given(repository.findByEmailIgnoreCase(dados.email())).willReturn(java.util.Optional.of(outro));

        //Act
        var ex = assertThrows(ValidacaoException.class, () -> service.atualizar(dados));

        //Assert
        assertEquals("E-mail ja cadastrado para outro cliente!", ex.getMessage());
    }

    @Test
    void deveriaExcluirClienteSemMaquinas() {
        //Arrange
        given(repository.existsById(1L)).willReturn(true);
        given(maquinaRepository.existsByClienteId(1L)).willReturn(false);

        //Act
        service.excluir(1L);

        //Assert
        then(repository).should().deleteById(1L);
    }

    @Test
    void naoDeveriaExcluirClienteComMaquinasCadastradas() {
        //Arrange
        given(repository.existsById(1L)).willReturn(true);
        given(maquinaRepository.existsByClienteId(1L)).willReturn(true);

        //Act
        var ex = assertThrows(ValidacaoException.class, () -> service.excluir(1L));

        //Assert
        then(repository).should(never()).deleteById(any());
        assertEquals("Cliente nao pode ser excluido pois possui maquinas cadastradas!", ex.getMessage());
    }
}
