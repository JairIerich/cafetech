package com.cafetech.api.domain.maquina;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaquinaRepository extends JpaRepository<Maquina, Long> {

    boolean existsByNumeroSerieIgnoreCase(String numeroSerie);

    Optional<Maquina> findByNumeroSerieIgnoreCase(String numeroSerie);

    boolean existsByClienteId(Long clienteId);

    Page<Maquina> findAllByClienteId(Long clienteId, Pageable paginacao);

    Page<Maquina> findAllByStatus(StatusMaquina status, Pageable paginacao);

    Page<Maquina> findAllByClienteIdAndStatus(Long clienteId, StatusMaquina status, Pageable paginacao);
}
