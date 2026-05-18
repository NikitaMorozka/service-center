package ru.servicecenter.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecenter.server.domain.entity.Client;
import ru.servicecenter.server.dto.client.ClientRequest;
import ru.servicecenter.server.dto.client.ClientResponse;
import ru.servicecenter.server.exception.ResourceNotFoundException;
import ru.servicecenter.server.mapper.ClientMapper;
import ru.servicecenter.server.repository.ClientRepository;
import ru.servicecenter.server.specification.ClientSpecifications;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Transactional(readOnly = true)
    public List<ClientResponse> findAll(String search) {
        Specification<Client> spec = ClientSpecifications.search(search);
        return clientRepository.findAll(spec).stream().map(clientMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ClientResponse findById(Long id) {
        return clientMapper.toResponse(getClient(id));
    }

    @Transactional
    public ClientResponse create(ClientRequest request) {
        Client client = clientMapper.toEntity(request);
        return clientMapper.toResponse(clientRepository.save(client));
    }

    @Transactional
    public ClientResponse update(Long id, ClientRequest request) {
        Client client = getClient(id);
        clientMapper.updateEntity(request, client);
        return clientMapper.toResponse(clientRepository.save(client));
    }

    @Transactional
    public void delete(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Клиент не найден: " + id);
        }
        clientRepository.deleteById(id);
    }

    private Client getClient(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Клиент не найден: " + id));
    }
}
