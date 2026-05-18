package ru.servicecenter.client.service;

import ru.servicecenter.client.api.ApiClient;
import ru.servicecenter.client.api.ApiException;
import ru.servicecenter.client.dto.AuthResponse;
import ru.servicecenter.client.dto.LoginRequest;

import java.io.IOException;

public class AuthApiService {

    private final ApiClient api = ApiClient.getInstance();

    public AuthResponse login(String username, String password) throws ApiException, IOException, InterruptedException {
        return api.post("/api/auth/login", new LoginRequest(username, password), AuthResponse.class);
    }
}
