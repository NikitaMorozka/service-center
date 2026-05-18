package ru.servicecenter.server.dto.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientRequest {
    @NotBlank
    @Size(max = 200)
    private String fullName;

    @NotBlank
    @Size(max = 50)
    private String phone;

    @Email
    @Size(max = 200)
    private String email;

    @Size(max = 500)
    private String address;

    private String notes;
}
