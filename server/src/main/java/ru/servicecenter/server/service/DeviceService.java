package ru.servicecenter.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecenter.server.domain.entity.Client;
import ru.servicecenter.server.domain.entity.Device;
import ru.servicecenter.server.dto.device.DeviceRequest;
import ru.servicecenter.server.dto.device.DeviceResponse;
import ru.servicecenter.server.exception.ResourceNotFoundException;
import ru.servicecenter.server.mapper.DeviceMapper;
import ru.servicecenter.server.repository.ClientRepository;
import ru.servicecenter.server.repository.DeviceRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final ClientRepository clientRepository;
    private final DeviceMapper deviceMapper;

    @Transactional(readOnly = true)
    public List<DeviceResponse> findAll(Long clientId) {
        List<Device> devices = clientId == null
                ? deviceRepository.findAllWithClient()
                : deviceRepository.findByClientId(clientId);
        return devices.stream().map(deviceMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DeviceResponse findById(Long id) {
        return deviceMapper.toResponse(getDevice(id));
    }

    @Transactional
    public DeviceResponse create(DeviceRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Клиент не найден: " + request.getClientId()));

        Device device = Device.builder()
                .client(client)
                .brand(request.getBrand())
                .model(request.getModel())
                .serialNumber(request.getSerialNumber())
                .deviceType(request.getDeviceType())
                .description(request.getDescription())
                .build();

        return deviceMapper.toResponse(deviceRepository.save(device));
    }

    @Transactional
    public DeviceResponse update(Long id, DeviceRequest request) {
        Device device = getDevice(id);
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Клиент не найден: " + request.getClientId()));

        device.setClient(client);
        device.setBrand(request.getBrand());
        device.setModel(request.getModel());
        device.setSerialNumber(request.getSerialNumber());
        device.setDeviceType(request.getDeviceType());
        device.setDescription(request.getDescription());

        return deviceMapper.toResponse(deviceRepository.save(device));
    }

    @Transactional
    public void delete(Long id) {
        if (!deviceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Устройство не найдено: " + id);
        }
        deviceRepository.deleteById(id);
    }

    private Device getDevice(Long id) {
        return deviceRepository.findByIdWithClient(id)
                .orElseThrow(() -> new ResourceNotFoundException("Устройство не найдено: " + id));
    }
}
