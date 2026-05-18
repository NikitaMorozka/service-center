package ru.servicecenter.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.servicecenter.client.config.AppConfig;
import ru.servicecenter.client.dto.ApiError;
import ru.servicecenter.client.session.Session;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

public class ApiClient {

    private static final ApiClient INSTANCE = new ApiClient();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static ApiClient getInstance() {
        return INSTANCE;
    }

    public <T> T get(String path, Class<T> type) throws ApiException, IOException, InterruptedException {
        return send("GET", path, null, type);
    }

    public <T> T getList(String path, TypeReference<T> typeRef) throws ApiException, IOException, InterruptedException {
        return send("GET", path, null, typeRef);
    }

    public <T> T post(String path, Object body, Class<T> type) throws ApiException, IOException, InterruptedException {
        return send("POST", path, body, type);
    }

    public <T> T put(String path, Object body, Class<T> type) throws ApiException, IOException, InterruptedException {
        return send("PUT", path, body, type);
    }

    public <T> T patch(String path, Object body, Class<T> type) throws ApiException, IOException, InterruptedException {
        return send("PATCH", path, body, type);
    }

    public void delete(String path) throws ApiException, IOException, InterruptedException {
        send("DELETE", path, null, Void.class);
    }

    private <T> T send(String method, String path, Object body, Class<T> type)
            throws ApiException, IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.API_BASE_URL + path))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json");

        String token = Session.getToken();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }

        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.noBody();
        if (body != null) {
            bodyPublisher = HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body));
        }

        builder.method(method, bodyPublisher);

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            String message = extractErrorMessage(response.body()).orElse("HTTP " + response.statusCode());
            throw new ApiException(message);
        }

        if (type == Void.class || response.body() == null || response.body().isBlank()) {
            return null;
        }
        return mapper.readValue(response.body(), type);
    }

    private <T> T send(String method, String path, Object body, TypeReference<T> typeRef)
            throws ApiException, IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.API_BASE_URL + path))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json");

        String token = Session.getToken();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }

        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.noBody();
        if (body != null) {
            bodyPublisher = HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body));
        }
        builder.method(method, bodyPublisher);

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            String message = extractErrorMessage(response.body()).orElse("HTTP " + response.statusCode());
            throw new ApiException(message);
        }
        return mapper.readValue(response.body(), typeRef);
    }

    private Optional<String> extractErrorMessage(String body) {
        try {
            ApiError error = mapper.readValue(body, ApiError.class);
            return Optional.ofNullable(error.getMessage());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
