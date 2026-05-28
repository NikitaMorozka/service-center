package ru.servicecenter.client.config;

public final class AppConfig {
    public static final String API_BASE_URL = System.getProperty("sc.api.base-url", "http://localhost:8080");

    private AppConfig() {
    }
}
