package ru.servicecenter.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecenter.server.domain.entity.Brand;
import ru.servicecenter.server.dto.catalog.CatalogItemRequest;
import ru.servicecenter.server.dto.catalog.CatalogItemResponse;
import ru.servicecenter.server.exception.BusinessException;
import ru.servicecenter.server.repository.BrandRepository;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandCatalogService {

    private final BrandRepository brandRepository;

    @Transactional(readOnly = true)
    public List<CatalogItemResponse> findAll() {
        return brandRepository.findAll().stream()
                .sorted(Comparator.comparing(Brand::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CatalogItemResponse create(CatalogItemRequest request) {
        String name = normalize(request.getName());
        if (brandRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("Такой бренд уже есть: " + name);
        }
        Brand saved = brandRepository.save(Brand.builder().name(name).build());
        return toResponse(saved);
    }

    private CatalogItemResponse toResponse(Brand entity) {
        return CatalogItemResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    private static String normalize(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("Укажите название");
        }
        return name.trim();
    }
}
