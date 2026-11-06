package com.cafetech.api.domain.maquina;

import com.cafetech.api.domain.cliente.Cliente;
import com.cafetech.api.domain.manutencao.Manutencao;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Table(name = "maquinas")
@Entity(name = "Maquina")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Maquina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String modelo;

    @Column(name = "numero_serie", nullable = false, length = 40)
    private String numeroSerie;

    @Column(name = "data_aquisicao")
    private LocalDate dataAquisicao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusMaquina status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToMany(mappedBy = "maquina")
    private List<Manutencao> manutencoes = new ArrayList<>();

    public Maquina(DadosCadastroMaquina dados, Cliente cliente) {
        this.modelo = dados.modelo();
        this.numeroSerie = dados.numeroSerie();
        this.dataAquisicao = dados.dataAquisicao();
        this.status = dados.status() == null ? StatusMaquina.ATIVA : dados.status();
        this.cliente = cliente;
    }

    public void atualizarInformacoes(DadosAtualizacaoMaquina dados, Cliente cliente) {
        if (dados.modelo() != null) {
            this.modelo = dados.modelo();
        }
        if (dados.numeroSerie() != null) {
            this.numeroSerie = dados.numeroSerie();
        }
        if (dados.dataAquisicao() != null) {
            this.dataAquisicao = dados.dataAquisicao();
        }
        if (dados.status() != null) {
            this.status = dados.status();
        }
        if (cliente != null) {
            this.cliente = cliente;
        }
    }

    public void setStatus(StatusMaquina status) {
        this.status = status;
    }
}
