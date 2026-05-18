package ru.servicecenter.server.dto.client;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ClientResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private String notes;
    private Instant createdAt;
}
