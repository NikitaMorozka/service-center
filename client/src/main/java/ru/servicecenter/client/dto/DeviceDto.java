package ru.servicecenter.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceDto {
    private Long id;
    private Long clientId;
    private String clientName;
    private String brand;
    private String model;
    private String serialNumber;
    private String deviceType;
    private String description;
}
