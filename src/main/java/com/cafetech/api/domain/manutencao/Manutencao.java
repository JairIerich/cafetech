package com.cafetech.api.domain.manutencao;

import com.cafetech.api.domain.maquina.Maquina;
import com.cafetech.api.domain.maquina.StatusMaquina;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Table(name = "manutencoes")
@Entity(name = "Manutencao")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Manutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maquina_id", nullable = false)
    private Maquina maquina;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoManutencao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusManutencao status;

    @Column(name = "data_abertura", nullable = false)
    private LocalDate dataAbertura;

    @Column(name = "data_conclusao")
    private LocalDate dataConclusao;

    @Column(length = 80)
    private String tecnico;

    public Manutencao(DadosCadastroManutencao dados, Maquina maquina) {
        this.maquina = maquina;
        this.descricao = dados.descricao();
        this.tipo = dados.tipo();
        this.tecnico = dados.tecnico();
        this.dataAbertura = dados.dataAbertura() == null ? LocalDate.now() : dados.dataAbertura();
        this.status = StatusManutencao.ABERTA;
    }

    public void iniciar() {
        this.status = StatusManutencao.EM_ANDAMENTO;
    }

    public void concluir(LocalDate dataConclusao) {
        this.status = StatusManutencao.CONCLUIDA;
        this.dataConclusao = dataConclusao;
    }
}
