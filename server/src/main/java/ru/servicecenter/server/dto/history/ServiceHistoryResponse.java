package ru.servicecenter.server.dto.history;

import lombok.Builder;
import lombok.Data;
import ru.servicecenter.server.domain.enums.RepairStatus;

import java.time.Instant;

@Data
@Builder
public class ServiceHistoryResponse {
    private Long id;
    private RepairStatus oldStatus;
    private RepairStatus newStatus;
    private String changedByName;
    private String comment;
    private Instant createdAt;
}
