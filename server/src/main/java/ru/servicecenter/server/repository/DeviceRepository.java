package ru.servicecenter.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.servicecenter.server.domain.entity.Device;

import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long>, JpaSpecificationExecutor<Device> {
    @Query("SELECT d FROM Device d JOIN FETCH d.client WHERE d.client.id = :clientId")
    List<Device> findByClientId(Long clientId);

    @Query("SELECT d FROM Device d JOIN FETCH d.client")
    List<Device> findAllWithClient();

    @Query("SELECT d FROM Device d JOIN FETCH d.client WHERE d.id = :id")
    Optional<Device> findByIdWithClient(Long id);
}
