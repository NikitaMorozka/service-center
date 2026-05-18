package ru.servicecenter.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.servicecenter.server.dto.catalog.CatalogItemRequest;
import ru.servicecenter.server.dto.catalog.CatalogItemResponse;
import ru.servicecenter.server.service.DeviceTypeCatalogService;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/device-types")
@RequiredArgsConstructor
public class DeviceTypeCatalogController {

    private final DeviceTypeCatalogService deviceTypeCatalogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<CatalogItemResponse> findAll() {
        return deviceTypeCatalogService.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CatalogItemResponse create(@Valid @RequestBody CatalogItemRequest request) {
        return deviceTypeCatalogService.create(request);
    }
}
