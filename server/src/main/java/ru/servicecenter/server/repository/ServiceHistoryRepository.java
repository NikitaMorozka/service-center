package ru.servicecenter.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.servicecenter.server.domain.entity.ServiceHistory;

import java.util.List;

public interface ServiceHistoryRepository extends JpaRepository<ServiceHistory, Long> {
    List<ServiceHistory> findByRepairRequestIdOrderByCreatedAtDesc(Long repairRequestId);
}
