package ru.servicecenter.server.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.servicecenter.server.domain.enums.RoleName;

@Data
public class UserRequest {
    @NotBlank
    @Size(max = 100)
    private String username;

    @Size(min = 6, max = 100)
    private String password;

    @NotBlank
    @Size(max = 200)
    private String fullName;

    @Email
    @Size(max = 200)
    private String email;

    @NotNull
    private RoleName role;

    private boolean active = true;
}
