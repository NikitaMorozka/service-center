package ru.servicecenter.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepairDto {
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
    private String status;
    private String problemDescription;
    private String diagnosis;
    private BigDecimal estimatedCost;
    private BigDecimal finalCost;
}
