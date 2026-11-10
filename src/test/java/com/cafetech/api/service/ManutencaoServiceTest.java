package com.cafetech.api.service;

import com.cafetech.api.domain.cliente.Cliente;
import com.cafetech.api.domain.cliente.DadosCadastroCliente;
import com.cafetech.api.domain.maquina.Maquina;
import com.cafetech.api.domain.maquina.DadosCadastroMaquina;
import com.cafetech.api.domain.maquina.MaquinaRepository;
import com.cafetech.api.domain.maquina.StatusMaquina;
import com.cafetech.api.domain.manutencao.DadosAndamentoManutencao;
import com.cafetech.api.domain.manutencao.DadosCadastroManutencao;
import com.cafetech.api.domain.manutencao.DadosConclusaoManutencao;
import com.cafetech.api.domain.manutencao.DadosDetalhamentoManutencao;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class ManutencaoServiceTest {

    @InjectMocks
    private ManutencaoService service;

    @Mock
    private ManutencaoRepository repository;

    @Mock
    private MaquinaRepository maquinaRepository;

    @Test
    void deveriaAbrirManutencaoEMaquinaFicaEmManutencao() {
        //Arrange
        var cliente = new Cliente(new DadosCadastroCliente("Maria Silva", "maria@cafe.com", null, null));
        var maquina = new Maquina(new DadosCadastroMaquina("Coffee Master 3000", "CMX-0001",
                null, StatusMaquina.ATIVA, 1L), cliente);
        var dados = new DadosCadastroManutencao(1L, "Bombas com vazamento", TipoManutencao.CORRETIVA, "Carlos", null);
        given(maquinaRepository.findById(dados.maquinaId())).willReturn(Optional.of(maquina));
        given(repository.existsByMaquinaIdAndStatusNot(maquina.getId(), StatusManutencao.CONCLUIDA)).willReturn(false);

        //Act
        var dto = service.abrir(dados);

        //Assert
        then(repository).should().save(new Manutencao(dados, maquina));
        assertEquals(StatusManutencao.ABERTA, dto.status());
        assertEquals(StatusMaquina.EM_MANUTENCAO, maquina.getStatus());
    }

    @Test
    void naoDeveriaAbrirManutencaoDeMaquinaInexistente() {
        //Arrange
        var dados = new DadosCadastroManutencao(99L, "Limpeza", TipoManutencao.PREVENTIVA, "Carlos", null);
        given(maquinaRepository.findById(dados.maquinaId())).willReturn(Optional.empty());

        //Act //Assert
        assertThrows(EntityNotFoundException.class, () -> service.abrir(dados));
        then(repository).should(never()).save(any());
    }

    @Test
    void naoDeveriaAbrirManutencaoDeMaquinaInativa() {
        //Arrange
        var cliente = new Cliente(new DadosCadastroCliente("Maria Silva", "maria@cafe.com", null, null));
        var maquina = new Maquina(new DadosCadastroMaquina("Coffee Master 3000", "CMX-0001",
                null, StatusMaquina.INATIVA, 1L), cliente);
        var dados = new DadosCadastroManutencao(1L, "Limpeza", TipoManutencao.PREVENTIVA, "Carlos", null);
        given(maquinaRepository.findById(dados.maquinaId())).willReturn(Optional.of(maquina));

        //Act
        var ex = assertThrows(ValidacaoException.class, () -> service.abrir(dados));

        //Assert
        assertEquals("Nao e possivel abrir manutencao para uma maquina inativa!", ex.getMessage());
    }

    @Test
    void naoDeveriaAbrirManutencaoQuandoJaExisteEmAberto() {
        //Arrange
        var cliente = new Cliente(new DadosCadastroCliente("Maria Silva", "maria@cafe.com", null, null));
        var maquina = new Maquina(new DadosCadastroMaquina("Coffee Master 3000", "CMX-0001",
                null, StatusMaquina.ATIVA, 1L), cliente);
        var dados = new DadosCadastroManutencao(1L, "Limpeza", TipoManutencao.PREVENTIVA, "Carlos", null);
        given(maquinaRepository.findById(dados.maquinaId())).willReturn(Optional.of(maquina));
        given(repository.existsByMaquinaIdAndStatusNot(maquina.getId(), StatusManutencao.CONCLUIDA)).willReturn(true);

        //Act
        var ex = assertThrows(ValidacaoException.class, () -> service.abrir(dados));

        //Assert
        then(repository).should(never()).save(any());
        assertEquals("Maquina ja possui manutencao em aberto!", ex.getMessage());
    }

    @Test
    void deveriaIniciarManutencaoEmAndamento() {
        //Arrange
        var cliente = new Cliente(new DadosCadastroCliente("Maria Silva", "maria@cafe.com", null, null));
        var maquina = new Maquina(new DadosCadastroMaquina("Coffee Master 3000", "CMX-0001",
                null, StatusMaquina.ATIVA, 1L), cliente);
        var manutencao = new Manutencao(new DadosCadastroManutencao(1L, "Limpeza", TipoManutencao.PREVENTIVA, "Carlos", null), maquina);
        var dados = new DadosAndamentoManutencao(1L);
        given(repository.findById(dados.id())).willReturn(Optional.of(manutencao));

        //Act
        var dto = service.iniciarEmAndamento(dados);

        //Assert
        assertEquals(StatusManutencao.EM_ANDAMENTO, dto.status());
    }

    @Test
    void naoDeveriaIniciarManutencaoQueNaoEstaAberta() {
        //Arrange
        var cliente = new Cliente(new DadosCadastroCliente("Maria Silva", "maria@cafe.com", null, null));
        var maquina = new Maquina(new DadosCadastroMaquina("Coffee Master 3000", "CMX-0001",
                null, StatusMaquina.ATIVA, 1L), cliente);
        var manutencao = new Manutencao(new DadosCadastroManutencao(1L, "Limpeza", TipoManutencao.PREVENTIVA, "Carlos", null), maquina);
        manutencao.iniciar();
        var dados = new DadosAndamentoManutencao(1L);
        given(repository.findById(dados.id())).willReturn(Optional.of(manutencao));

        //Act
        var ex = assertThrows(ValidacaoException.class, () -> service.iniciarEmAndamento(dados));

        //Assert
        assertEquals("Somente manutencao ABERTA pode ir para EM_ANDAMENTO!", ex.getMessage());
    }

    @Test
    void deveriaConcluirManutencaoEMaquinaVoltaAtiva() {
        //Arrange
        var cliente = new Cliente(new DadosCadastroCliente("Maria Silva", "maria@cafe.com", null, null));
        var maquina = new Maquina(new DadosCadastroMaquina("Coffee Master 3000", "CMX-0001",
                null, StatusMaquina.ATIVA, 1L), cliente);
        var dadosAbertura = new DadosCadastroManutencao(1L, "Bombas", TipoManutencao.CORRETIVA, "Carlos", null);
        var manutencao = new Manutencao(dadosAbertura, maquina);
        maquina.setStatus(StatusMaquina.EM_MANUTENCAO);
        var conclusao = LocalDate.now().plusDays(2);
        var dados = new DadosConclusaoManutencao(1L, conclusao);
        given(repository.findById(dados.id())).willReturn(Optional.of(manutencao));
        given(repository.existsByMaquinaIdAndStatusNot(maquina.getId(), StatusManutencao.CONCLUIDA)).willReturn(false);

        //Act
        var dto = service.concluir(dados);

        //Assert
        assertEquals(StatusManutencao.CONCLUIDA, dto.status());
        assertEquals(conclusao, dto.dataConclusao());
        assertEquals(StatusMaquina.ATIVA, maquina.getStatus());
    }

    @Test
    void naoDeveriaConcluirComDataAnteriorAAbertura() {
        //Arrange
        var cliente = new Cliente(new DadosCadastroCliente("Maria Silva", "maria@cafe.com", null, null));
        var maquina = new Maquina(new DadosCadastroMaquina("Coffee Master 3000", "CMX-0001",
                null, StatusMaquina.ATIVA, 1L), cliente);
        var abertura = LocalDate.of(2026, 10, 28);
        var dadosAbertura = new DadosCadastroManutencao(1L, "Bombas", TipoManutencao.CORRETIVA, "Carlos", abertura);
        var manutencao = new Manutencao(dadosAbertura, maquina);
        var dados = new DadosConclusaoManutencao(1L, abertura.minusDays(1));
        given(repository.findById(dados.id())).willReturn(Optional.of(manutencao));

        //Act
        var ex = assertThrows(ValidacaoException.class, () -> service.concluir(dados));

        //Assert
        assertEquals("Data de conclusao nao pode ser anterior a data de abertura!", ex.getMessage());
    }

    @Test
    void naoDeveriaConcluirManutencaoJaConcluida() {
        //Arrange
        var cliente = new Cliente(new DadosCadastroCliente("Maria Silva", "maria@cafe.com", null, null));
        var maquina = new Maquina(new DadosCadastroMaquina("Coffee Master 3000", "CMX-0001",
                null, StatusMaquina.ATIVA, 1L), cliente);
        var dadosAbertura = new DadosCadastroManutencao(1L, "Bombas", TipoManutencao.CORRETIVA, "Carlos", null);
        var manutencao = new Manutencao(dadosAbertura, maquina);
        manutencao.concluir(LocalDate.now());
        var dados = new DadosConclusaoManutencao(1L, null);
        given(repository.findById(dados.id())).willReturn(Optional.of(manutencao));

        //Act
        var ex = assertThrows(ValidacaoException.class, () -> service.concluir(dados));

        //Assert
        assertEquals("Manutencao ja esta CONCLUIDA!", ex.getMessage());
    }
}
