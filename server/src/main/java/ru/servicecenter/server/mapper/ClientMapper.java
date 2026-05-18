package ru.servicecenter.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.servicecenter.server.domain.entity.Client;
import ru.servicecenter.server.dto.client.ClientRequest;
import ru.servicecenter.server.dto.client.ClientResponse;

@Mapper(componentModel = "spring")
public interface ClientMapper {
    ClientResponse toResponse(Client client);

    Client toEntity(ClientRequest request);

    void updateEntity(ClientRequest request, @MappingTarget Client client);
}
