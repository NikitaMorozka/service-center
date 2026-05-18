package ru.servicecenter.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.servicecenter.server.domain.entity.Device;
import ru.servicecenter.server.dto.device.DeviceResponse;

@Mapper(componentModel = "spring")
public interface DeviceMapper {
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientName", source = "client.fullName")
    DeviceResponse toResponse(Device device);
}
