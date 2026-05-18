package ru.servicecenter.server.dto.device;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class DeviceResponse {
    private Long id;
    private Long clientId;
    private String clientName;
    private String brand;
    private String model;
    private String serialNumber;
    private String deviceType;
    private String description;
    private Instant createdAt;
}
