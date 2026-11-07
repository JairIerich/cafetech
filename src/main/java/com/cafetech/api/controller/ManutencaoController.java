package com.cafetech.api.controller;

import com.cafetech.api.domain.manutencao.DadosAndamentoManutencao;
import com.cafetech.api.domain.manutencao.DadosCadastroManutencao;
import com.cafetech.api.domain.manutencao.DadosConclusaoManutencao;
import com.cafetech.api.domain.manutencao.DadosDetalhamentoManutencao;
import com.cafetech.api.domain.manutencao.DadosListagemManutencao;
import com.cafetech.api.domain.manutencao.StatusManutencao;
import com.cafetech.api.service.ManutencaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/manutencoes")
public class ManutencaoController {

    @Autowired
    private ManutencaoService service;

    @PostMapping
    @Transactional
    public ResponseEntity cadastrar(@RequestBody @Valid DadosCadastroManutencao dados, UriComponentsBuilder uriBuilder) {
        var dto = service.abrir(dados);
        var uri = uriBuilder.path("/manutencoes/{id}").buildAndExpand(dto.id()).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @GetMapping
    public ResponseEntity<Page<DadosListagemManutencao>> listar(
            @RequestParam(required = false) Long maquinaId,
            @RequestParam(required = false) StatusManutencao status,
            @PageableDefault(size = 10, sort = {"dataAbertura"}) Pageable paginacao) {
        return ResponseEntity.ok(service.listar(maquinaId, status, paginacao));
    }

    @PutMapping("/andamento")
    @Transactional
    public ResponseEntity iniciarEmAndamento(@RequestBody @Valid DadosAndamentoManutencao dados) {
        return ResponseEntity.ok(service.iniciarEmAndamento(dados));
    }

    @PutMapping("/conclusao")
    @Transactional
    public ResponseEntity concluir(@RequestBody @Valid DadosConclusaoManutencao dados) {
        return ResponseEntity.ok(service.concluir(dados));
    }
}
