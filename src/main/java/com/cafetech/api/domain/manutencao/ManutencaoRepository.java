package com.cafetech.api.domain.manutencao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManutencaoRepository extends JpaRepository<Manutencao, Long> {

    Page<Manutencao> findAllByMaquinaId(Long maquinaId, Pageable paginacao);

    Page<Manutencao> findAllByStatus(StatusManutencao status, Pageable paginacao);

    boolean existsByMaquinaIdAndStatusNot(Long maquinaId, StatusManutencao status);

    void deleteByMaquinaId(Long maquinaId);
}
