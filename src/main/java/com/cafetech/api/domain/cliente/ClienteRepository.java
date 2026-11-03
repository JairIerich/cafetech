package com.cafetech.api.domain.cliente;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<Cliente> findByEmailIgnoreCase(String email);
}
