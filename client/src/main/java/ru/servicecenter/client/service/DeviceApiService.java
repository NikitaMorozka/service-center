package ru.servicecenter.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import ru.servicecenter.client.api.ApiClient;
import ru.servicecenter.client.api.ApiException;
import ru.servicecenter.client.dto.DeviceDto;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceApiService {

    private final ApiClient api = ApiClient.getInstance();

    public List<DeviceDto> findAll(Long clientId) throws ApiException, IOException, InterruptedException {
        String path = clientId == null ? "/api/devices" : "/api/devices?clientId=" + clientId;
        return api.getList(path, new TypeReference<>() {});
    }

    public DeviceDto create(Map<String, Object> body) throws ApiException, IOException, InterruptedException {
        return api.post("/api/devices", body, DeviceDto.class);
    }

    public DeviceDto update(Long id, Map<String, Object> body) throws ApiException, IOException, InterruptedException {
        return api.put("/api/devices/" + id, body, DeviceDto.class);
    }

    public void delete(Long id) throws ApiException, IOException, InterruptedException {
        api.delete("/api/devices/" + id);
    }

    public static Map<String, Object> toBody(DeviceDto dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("clientId", dto.getClientId());
        map.put("brand", dto.getBrand());
        map.put("model", dto.getModel());
        map.put("serialNumber", dto.getSerialNumber());
        map.put("deviceType", dto.getDeviceType());
        map.put("description", dto.getDescription());
        return map;
    }
}
