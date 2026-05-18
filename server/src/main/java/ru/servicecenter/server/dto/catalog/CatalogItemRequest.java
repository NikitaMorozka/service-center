package ru.servicecenter.server.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CatalogItemRequest {

    @NotBlank
    @Size(max = 100)
    private String name;
}
