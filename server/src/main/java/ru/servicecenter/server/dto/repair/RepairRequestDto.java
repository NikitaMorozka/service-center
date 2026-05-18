package ru.servicecenter.server.dto.repair;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RepairRequestDto {
    @NotNull
    private Long clientId;

    @NotNull
    private Long deviceId;

    private Long masterId;

    @NotBlank
    private String problemDescription;

    private String diagnosis;

    private BigDecimal estimatedCost;
}
