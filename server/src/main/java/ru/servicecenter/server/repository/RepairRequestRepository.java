package ru.servicecenter.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.servicecenter.server.domain.entity.RepairRequest;

import java.util.List;
import java.util.Optional;

public interface RepairRequestRepository extends JpaRepository<RepairRequest, Long>, JpaSpecificationExecutor<RepairRequest> {
    boolean existsByRequestNumber(String requestNumber);

    /** Максимальный числовой суффикс для номеров вида SC-2026-00001 */
    @Query(value = """
            SELECT COALESCE(MAX(CAST(SUBSTRING(request_number FROM LENGTH(:prefix) + 1) AS INTEGER)), 0)
            FROM repair_requests
            WHERE request_number LIKE CONCAT(:prefix, '%')
            """, nativeQuery = true)
    int findMaxRequestSuffix(@Param("prefix") String prefix);

    @Query("SELECT r FROM RepairRequest r " +
            "JOIN FETCH r.client JOIN FETCH r.device LEFT JOIN FETCH r.master WHERE r.id = :id")
    Optional<RepairRequest> findByIdWithDetails(Long id);
}
