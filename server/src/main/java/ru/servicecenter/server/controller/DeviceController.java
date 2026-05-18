package ru.servicecenter.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.servicecenter.server.dto.device.DeviceRequest;
import ru.servicecenter.server.dto.device.DeviceResponse;
import ru.servicecenter.server.service.DeviceService;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<DeviceResponse> findAll(@RequestParam(required = false) Long clientId) {
        return deviceService.findAll(clientId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public DeviceResponse findById(@PathVariable Long id) {
        return deviceService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public DeviceResponse create(@Valid @RequestBody DeviceRequest request) {
        return deviceService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DeviceResponse update(@PathVariable Long id, @Valid @RequestBody DeviceRequest request) {
        return deviceService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        deviceService.delete(id);
    }
}
