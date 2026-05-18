package ru.servicecenter.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import ru.servicecenter.client.api.ApiClient;
import ru.servicecenter.client.api.ApiException;
import ru.servicecenter.client.dto.UserDto;
import ru.servicecenter.client.dto.UserRequest;

import java.io.IOException;
import java.util.List;

public class UserApiService {

    private final ApiClient api = ApiClient.getInstance();

    public List<UserDto> findAll() throws ApiException, IOException, InterruptedException {
        return api.getList("/api/admin/users", new TypeReference<>() {});
    }

    public UserDto create(UserRequest request) throws ApiException, IOException, InterruptedException {
        return api.post("/api/admin/users", request, UserDto.class);
    }

    public UserDto update(Long id, UserRequest request) throws ApiException, IOException, InterruptedException {
        return api.put("/api/admin/users/" + id, request, UserDto.class);
    }

    public void delete(Long id) throws ApiException, IOException, InterruptedException {
        api.delete("/api/admin/users/" + id);
    }

    public List<UserDto> findMasters() throws ApiException, IOException, InterruptedException {
        return api.getList("/api/masters", new TypeReference<>() {});
    }
}
