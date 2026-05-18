package ru.servicecenter.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.servicecenter.server.domain.entity.User;
import ru.servicecenter.server.dto.user.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "role", source = "role.name")
    UserResponse toResponse(User user);
}
