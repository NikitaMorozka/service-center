package ru.servicecenter.server.dto.repair;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.servicecenter.server.domain.enums.RepairStatus;

import java.math.BigDecimal;

@Data
public class StatusUpdateRequest {
    @NotNull
    private RepairStatus status;

    private String comment;

    private String diagnosis;

    private BigDecimal finalCost;
}
