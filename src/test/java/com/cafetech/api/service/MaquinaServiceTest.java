package com.cafetech.api.service;

import com.cafetech.api.domain.cliente.Cliente;
import com.cafetech.api.domain.cliente.ClienteRepository;
import com.cafetech.api.domain.cliente.DadosCadastroCliente;
import com.cafetech.api.domain.maquina.DadosCadastroMaquina;
import com.cafetech.api.domain.maquina.DadosDetalhamentoMaquina;
import com.cafetech.api.domain.maquina.Maquina;
import com.cafetech.api.domain.maquina.MaquinaRepository;
import com.cafetech.api.domain.maquina.StatusMaquina;
import com.cafetech.api.domain.manutencao.Manutencao;
import com.cafetech.api.domain.manutencao.ManutencaoRepository;
import com.cafetech.api.domain.manutencao.StatusManutencao;
import com.cafetech.api.domain.manutencao.TipoManutencao;
import com.cafetech.api.exception.ValidacaoException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class MaquinaServiceTest {

    @InjectMocks
    private MaquinaService service;

    @Mock
    private MaquinaRepository repository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ManutencaoRepository manutencaoRepository;

    @Test
    void deveriaCadastrarMaquinaComStatusAtivaPorPadrao() {
        //Arrange
        var cliente = new Cliente(new DadosCadastroCliente("Maria Silva", "maria@cafe.com", null, null));
        var dados = new DadosCadastroMaquina("Coffee Master 3000", "CMX-0001",
                LocalDate.of(2025, 3, 10), null, 1L);
        given(clienteRepository.findById(dados.clienteId())).willReturn(Optional.of(cliente));
        given(repository.existsByNumeroSerieIgnoreCase(dados.numeroSerie())).willReturn(false);

        //Act
        var dto = service.cadastrar(dados);

        //Assert
        then(repository).should().save(new Maquina(dados, cliente));
        assertEquals(StatusMaquina.ATIVA, dto.status());
        assertEquals("CMX-0001", dto.numeroSerie());
    }

    @Test
    void deveriaCadastrarMaquinaComStatusInformado() {
        //Arrange
        var cliente = new Cliente(new DadosCadastroCliente("Maria Silva", "maria@cafe.com", null, null));
        var dados = new DadosCadastroMaquina("Coffee Master 3000", "CMX-0002",
                null, StatusMaquina.INATIVA, 1L);
        given(clienteRepository.findById(dados.clienteId())).willReturn(Optional.of(cliente));
        given(repository.existsByNumeroSerieIgnoreCase(dados.numeroSerie())).willReturn(false);

        //Act
        var dto = service.cadastrar(dados);

        //Assert
        assertEquals(StatusMaquina.INATIVA, dto.status());
    }

    @Test
    void naoDeveriaCadastrarMaquinaDeClienteInexistente() {
        //Arrange
        var dados = new DadosCadastroMaquina("Coffee Master 3000", "CMX-0001", null, null, 1L);
        given(clienteRepository.findById(dados.clienteId())).willReturn(Optional.empty());

        //Act //Assert
        assertThrows(EntityNotFoundException.class, () -> service.cadastrar(dados));
        then(repository).should(never()).save(any());
    }

    @Test
    void naoDeveriaCadastrarMaquinaComNumeroSerieDuplicado() {
        //Arrange
        var cliente = new Cliente(new DadosCadastroCliente("Maria Silva", "maria@cafe.com", null, null));
        var dados = new DadosCadastroMaquina("Coffee Master 3000", "CMX-0001", null, null, 1L);
        given(clienteRepository.findById(dados.clienteId())).willReturn(Optional.of(cliente));
        given(repository.existsByNumeroSerieIgnoreCase(dados.numeroSerie())).willReturn(true);

        //Act
        var ex = assertThrows(ValidacaoException.class, () -> service.cadastrar(dados));

        //Assert
        then(repository).should(never()).save(any());
        assertEquals("Numero de serie ja cadastrado para outra maquina!", ex.getMessage());
    }

    @Test
    void deveriaExcluirMaquinaSemManutencaoEmAberto() {
        //Arrange
        given(repository.existsById(1L)).willReturn(true);
        given(manutencaoRepository.existsByMaquinaIdAndStatusNot(1L, StatusManutencao.CONCLUIDA)).willReturn(false);

        //Act
        service.excluir(1L);

        //Assert
        then(manutencaoRepository).should().deleteByMaquinaId(1L);
        then(repository).should().deleteById(1L);
    }

    @Test
    void naoDeveriaExcluirMaquinaComManutencaoEmAberto() {
        //Arrange
        var cliente = new Cliente(new DadosCadastroCliente("Maria Silva", "maria@cafe.com", null, null));
        var maquina = new Maquina(new DadosCadastroMaquina("Coffee Master 3000", "CMX-0001",
                null, StatusMaquina.ATIVA, 1L), cliente);
        given(repository.existsById(1L)).willReturn(true);
        given(manutencaoRepository.existsByMaquinaIdAndStatusNot(1L, StatusManutencao.CONCLUIDA)).willReturn(true);

        //Act
        var ex = assertThrows(ValidacaoException.class, () -> service.excluir(1L));

        //Assert
        then(repository).should(never()).deleteById(any());
        assertEquals("Maquina nao pode ser excluida pois possui manutencao em aberto!", ex.getMessage());
    }
}
