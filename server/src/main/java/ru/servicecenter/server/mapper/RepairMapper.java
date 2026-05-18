package ru.servicecenter.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.servicecenter.server.domain.entity.RepairRequest;
import ru.servicecenter.server.dto.repair.RepairResponse;

@Mapper(componentModel = "spring")
public interface RepairMapper {
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientName", source = "client.fullName")
    @Mapping(target = "clientPhone", source = "client.phone")
    @Mapping(target = "deviceId", source = "device.id")
    @Mapping(target = "deviceInfo", expression = "java(deviceInfo(entity))")
    @Mapping(target = "deviceType", source = "device.deviceType")
    @Mapping(target = "deviceBrand", source = "device.brand")
    @Mapping(target = "deviceModel", source = "device.model")
    @Mapping(target = "deviceSerial", source = "device.serialNumber")
    @Mapping(target = "masterId", source = "master.id")
    @Mapping(target = "masterName", source = "master.fullName")
    RepairResponse toResponse(RepairRequest entity);

    default String deviceInfo(RepairRequest entity) {
        if (entity.getDevice() == null) {
            return null;
        }
        return entity.getDevice().getBrand() + " " + entity.getDevice().getModel();
    }
}
