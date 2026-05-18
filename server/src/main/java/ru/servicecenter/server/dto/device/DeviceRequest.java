package ru.servicecenter.server.dto.device;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeviceRequest {
    @NotNull
    private Long clientId;

    @NotBlank
    @Size(max = 100)
    private String brand;

    @NotBlank
    @Size(max = 100)
    private String model;

    @Size(max = 100)
    private String serialNumber;

    @NotBlank
    @Size(max = 100)
    private String deviceType;

    private String description;
}
