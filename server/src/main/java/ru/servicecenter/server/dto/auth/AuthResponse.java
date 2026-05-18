package ru.servicecenter.server.dto.auth;

import lombok.Builder;
import lombok.Data;
import ru.servicecenter.server.domain.enums.RoleName;

@Data
@Builder
public class AuthResponse {
    private Long userId;
    private String token;
    private String username;
    private String fullName;
    private RoleName role;
}
