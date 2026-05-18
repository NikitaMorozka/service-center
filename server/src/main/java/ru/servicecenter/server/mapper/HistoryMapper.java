package ru.servicecenter.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.servicecenter.server.domain.entity.ServiceHistory;
import ru.servicecenter.server.dto.history.ServiceHistoryResponse;

@Mapper(componentModel = "spring")
public interface HistoryMapper {
    @Mapping(target = "changedByName", source = "changedBy.fullName")
    ServiceHistoryResponse toResponse(ServiceHistory history);
}
