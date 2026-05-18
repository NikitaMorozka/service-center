package ru.servicecenter.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import ru.servicecenter.client.api.ApiClient;
import ru.servicecenter.client.api.ApiException;
import ru.servicecenter.client.dto.CatalogItemDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CatalogApiService {

    private final ApiClient api = ApiClient.getInstance();

    public List<CatalogItemDto> findDeviceTypes() throws ApiException, IOException, InterruptedException {
        return api.getList("/api/catalog/device-types", new TypeReference<>() {});
    }

    public List<CatalogItemDto> findBrands() throws ApiException, IOException, InterruptedException {
        return api.getList("/api/catalog/brands", new TypeReference<>() {});
    }

    public CatalogItemDto createDeviceType(String name) throws ApiException, IOException, InterruptedException {
        return api.post("/api/catalog/device-types", Map.of("name", name), CatalogItemDto.class);
    }

    public CatalogItemDto createBrand(String name) throws ApiException, IOException, InterruptedException {
        return api.post("/api/catalog/brands", Map.of("name", name), CatalogItemDto.class);
    }
}
