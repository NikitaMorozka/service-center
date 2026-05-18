package ru.servicecenter.server.dto.repair;

import lombok.Builder;
import lombok.Data;
import ru.servicecenter.server.domain.enums.RepairStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class RepairResponse {
    private Long id;
    private String requestNumber;
    private Long clientId;
    private String clientName;
    private String clientPhone;
    private Long deviceId;
    private String deviceInfo;
    private String deviceType;
    private String deviceBrand;
    private String deviceModel;
    private String deviceSerial;
    private Long masterId;
    private String masterName;
    private RepairStatus status;
    private String problemDescription;
    private String diagnosis;
    private BigDecimal estimatedCost;
    private BigDecimal finalCost;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;
}
