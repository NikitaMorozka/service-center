package ru.servicecenter.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import ru.servicecenter.client.api.ApiClient;
import ru.servicecenter.client.api.ApiException;
import ru.servicecenter.client.dto.HistoryDto;
import ru.servicecenter.client.dto.RepairDto;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepairApiService {

    private final ApiClient api = ApiClient.getInstance();

    public List<RepairDto> findAll(boolean archived, String status, Long masterId, String search)
            throws ApiException, IOException, InterruptedException {
        StringBuilder path = new StringBuilder("/api/repairs?archived=").append(archived);
        if (status != null && !status.isBlank() && !"ALL".equals(status)) {
            path.append("&status=").append(status);
        }
        if (masterId != null) {
            path.append("&masterId=").append(masterId);
        }
        if (search != null && !search.isBlank()) {
            path.append("&search=")
                    .append(java.net.URLEncoder.encode(search, java.nio.charset.StandardCharsets.UTF_8));
        }
        return api.getList(path.toString(), new TypeReference<>() {});
    }

    public void delete(Long id) throws ApiException, IOException, InterruptedException {
        api.delete("/api/repairs/" + id);
    }

    public RepairDto create(Map<String, Object> body) throws ApiException, IOException, InterruptedException {
        return api.post("/api/repairs", body, RepairDto.class);
    }

    public RepairDto updateStatus(Long id, Map<String, Object> body) throws ApiException, IOException, InterruptedException {
        return api.patch("/api/repairs/" + id + "/status", body, RepairDto.class);
    }

    public RepairDto assignMaster(Long id, Long masterId) throws ApiException, IOException, InterruptedException {
        Map<String, Object> body = Map.of("masterId", masterId);
        return api.patch("/api/repairs/" + id + "/assign-master", body, RepairDto.class);
    }

    public List<HistoryDto> getHistory(Long id) throws ApiException, IOException, InterruptedException {
        return api.getList("/api/repairs/" + id + "/history", new TypeReference<>() {});
    }

    public static Map<String, Object> statusBody(String status, String comment, String diagnosis, Object finalCost) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        map.put("comment", comment);
        if (diagnosis != null) map.put("diagnosis", diagnosis);
        if (finalCost != null) map.put("finalCost", finalCost);
        return map;
    }
}
