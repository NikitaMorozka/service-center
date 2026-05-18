package ru.servicecenter.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.servicecenter.server.domain.enums.RepairStatus;
import ru.servicecenter.server.dto.history.ServiceHistoryResponse;
import ru.servicecenter.server.dto.repair.*;
import ru.servicecenter.server.security.CustomUserDetails;
import ru.servicecenter.server.service.RepairService;

import java.util.List;

@RestController
@RequestMapping("/api/repairs")
@RequiredArgsConstructor
public class RepairController {

    private final RepairService repairService;

    @GetMapping
    public List<RepairResponse> findAll(
            @RequestParam(required = false) Boolean archived,
            @RequestParam(required = false) RepairStatus status,
            @RequestParam(required = false) Long masterId,
            @RequestParam(required = false) String search
    ) {
        return repairService.findAll(archived, status, masterId, search);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        repairService.delete(id);
    }

    @GetMapping("/{id}")
    public RepairResponse findById(@PathVariable Long id) {
        return repairService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public RepairResponse create(@Valid @RequestBody RepairRequestDto request) {
        return repairService.create(request);
    }

    @PatchMapping("/{id}/status")
    public RepairResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return repairService.updateStatus(id, request, currentUser);
    }

    @PatchMapping("/{id}/assign-master")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public RepairResponse assignMaster(@PathVariable Long id, @Valid @RequestBody AssignMasterRequest request) {
        return repairService.assignMaster(id, request);
    }

    @GetMapping("/{id}/history")
    public List<ServiceHistoryResponse> getHistory(@PathVariable Long id) {
        return repairService.getHistory(id);
    }
}
