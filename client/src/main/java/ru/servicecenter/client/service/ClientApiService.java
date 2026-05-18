package ru.servicecenter.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import ru.servicecenter.client.api.ApiClient;
import ru.servicecenter.client.api.ApiException;
import ru.servicecenter.client.dto.ClientDto;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientApiService {

    private final ApiClient api = ApiClient.getInstance();

    public List<ClientDto> findAll(String search) throws ApiException, IOException, InterruptedException {
        String path = "/api/clients";
        if (search != null && !search.isBlank()) {
            path += "?search=" + java.net.URLEncoder.encode(search, java.nio.charset.StandardCharsets.UTF_8);
        }
        return api.getList(path, new TypeReference<>() {});
    }

    public ClientDto create(Map<String, Object> body) throws ApiException, IOException, InterruptedException {
        return api.post("/api/clients", body, ClientDto.class);
    }

    public ClientDto update(Long id, Map<String, Object> body) throws ApiException, IOException, InterruptedException {
        return api.put("/api/clients/" + id, body, ClientDto.class);
    }

    public void delete(Long id) throws ApiException, IOException, InterruptedException {
        api.delete("/api/clients/" + id);
    }

    public static Map<String, Object> toBody(ClientDto dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("fullName", dto.getFullName());
        map.put("phone", dto.getPhone());
        map.put("email", dto.getEmail());
        map.put("address", dto.getAddress());
        map.put("notes", dto.getNotes());
        return map;
    }
}
