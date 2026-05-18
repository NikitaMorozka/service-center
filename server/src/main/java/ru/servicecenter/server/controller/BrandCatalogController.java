package ru.servicecenter.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.servicecenter.server.dto.catalog.CatalogItemRequest;
import ru.servicecenter.server.dto.catalog.CatalogItemResponse;
import ru.servicecenter.server.service.BrandCatalogService;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/brands")
@RequiredArgsConstructor
public class BrandCatalogController {

    private final BrandCatalogService brandCatalogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<CatalogItemResponse> findAll() {
        return brandCatalogService.findAll();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public CatalogItemResponse create(@Valid @RequestBody CatalogItemRequest request) {
        return brandCatalogService.create(request);
    }
}
