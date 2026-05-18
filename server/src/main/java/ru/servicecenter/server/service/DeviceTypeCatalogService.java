package ru.servicecenter.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecenter.server.domain.entity.DeviceType;
import ru.servicecenter.server.dto.catalog.CatalogItemRequest;
import ru.servicecenter.server.dto.catalog.CatalogItemResponse;
import ru.servicecenter.server.exception.BusinessException;
import ru.servicecenter.server.repository.DeviceTypeRepository;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceTypeCatalogService {

    private final DeviceTypeRepository deviceTypeRepository;

    @Transactional(readOnly = true)
    public List<CatalogItemResponse> findAll() {
        return deviceTypeRepository.findAll().stream()
                .sorted(Comparator.comparing(DeviceType::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CatalogItemResponse create(CatalogItemRequest request) {
        String name = normalize(request.getName());
        if (deviceTypeRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("Такой тип устройства уже есть: " + name);
        }
        DeviceType saved = deviceTypeRepository.save(DeviceType.builder().name(name).build());
        return toResponse(saved);
    }

    private CatalogItemResponse toResponse(DeviceType entity) {
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
