package ru.servicecenter.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.servicecenter.server.domain.entity.Brand;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    Optional<Brand> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
