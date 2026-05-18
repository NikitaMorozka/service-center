package ru.servicecenter.server.dto.user;

import lombok.Builder;
import lombok.Data;
import ru.servicecenter.server.domain.enums.RoleName;

import java.time.Instant;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private boolean active;
    private RoleName role;
    private Instant createdAt;
}
