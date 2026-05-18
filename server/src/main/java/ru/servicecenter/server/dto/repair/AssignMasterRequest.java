package ru.servicecenter.server.dto.repair;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignMasterRequest {
    @NotNull
    private Long masterId;
}
