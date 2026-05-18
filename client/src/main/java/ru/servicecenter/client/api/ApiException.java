package ru.servicecenter.client.api;

public class ApiException extends Exception {
    public ApiException(String message) {
        super(message);
    }
}
