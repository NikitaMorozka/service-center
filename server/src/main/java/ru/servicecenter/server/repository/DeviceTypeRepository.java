package ru.servicecenter.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.servicecenter.server.domain.entity.DeviceType;

import java.util.Optional;

public interface DeviceTypeRepository extends JpaRepository<DeviceType, Long> {

    Optional<DeviceType> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
