package ru.servicecenter.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecenter.server.domain.entity.*;
import ru.servicecenter.server.domain.enums.RepairStatus;
import ru.servicecenter.server.domain.enums.RoleName;
import ru.servicecenter.server.dto.history.ServiceHistoryResponse;
import ru.servicecenter.server.dto.repair.*;
import ru.servicecenter.server.exception.BusinessException;
import ru.servicecenter.server.exception.ResourceNotFoundException;
import ru.servicecenter.server.mapper.HistoryMapper;
import ru.servicecenter.server.mapper.RepairMapper;
import ru.servicecenter.server.repository.*;
import ru.servicecenter.server.security.CustomUserDetails;
import ru.servicecenter.server.specification.RepairSpecifications;

import java.time.Instant;
import java.time.Year;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepairService {

    private final RepairRequestRepository repairRequestRepository;
    private final ClientRepository clientRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final ServiceHistoryRepository serviceHistoryRepository;
    private final RepairMapper repairMapper;
    private final HistoryMapper historyMapper;

    @Transactional(readOnly = true)
    public List<RepairResponse> findAll(Boolean archived, RepairStatus status, Long masterId, String search) {
        Specification<RepairRequest> spec = Specification
                .where(RepairSpecifications.isArchived(archived))
                .and(RepairSpecifications.hasStatus(status))
                .and(RepairSpecifications.hasMasterId(masterId))
                .and(RepairSpecifications.search(search));

        List<RepairRequest> repairs = repairRequestRepository.findAll(spec);
        return repairs.stream().map(this::toResponseWithFetch).toList();
    }

    @Transactional
    public void delete(Long id) {
        RepairRequest repair = getRepair(id);
        if (repair.getStatus() != RepairStatus.COMPLETED && repair.getStatus() != RepairStatus.CANCELED) {
            throw new BusinessException("Удалять можно только заявки из архива (завершённые или отменённые)");
        }
        repairRequestRepository.delete(repair);
    }

    @Transactional(readOnly = true)
    public RepairResponse findById(Long id) {
        return toResponseWithFetch(getRepair(id));
    }

    @Transactional
    public RepairResponse create(RepairRequestDto dto) {
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Клиент не найден"));
        Device device = deviceRepository.findById(dto.getDeviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Устройство не найдено"));

        if (!device.getClient().getId().equals(client.getId())) {
            throw new BusinessException("Устройство не принадлежит выбранному клиенту");
        }

        User master = null;
        if (dto.getMasterId() != null) {
            master = getMaster(dto.getMasterId());
        }

        RepairRequest repair = RepairRequest.builder()
                .requestNumber(generateRequestNumber())
                .client(client)
                .device(device)
                .master(master)
                .status(RepairStatus.NEW)
                .problemDescription(dto.getProblemDescription())
                .diagnosis(dto.getDiagnosis())
                .estimatedCost(dto.getEstimatedCost())
                .build();

        RepairRequest saved = repairRequestRepository.save(repair);
        addHistory(saved, null, RepairStatus.NEW, "Заявка создана", null);
        return toResponseWithFetch(getRepair(saved.getId()));
    }

    @Transactional
    public RepairResponse updateStatus(Long id, StatusUpdateRequest request, CustomUserDetails currentUser) {
        RepairRequest repair = getRepair(id);
        RepairStatus oldStatus = repair.getStatus();
        RepairStatus newStatus = request.getStatus();

        if (oldStatus == RepairStatus.COMPLETED || oldStatus == RepairStatus.CANCELED) {
            throw new BusinessException("Нельзя изменить статус завершённой или отменённой заявки");
        }

        repair.setStatus(newStatus);
        if (request.getDiagnosis() != null) {
            repair.setDiagnosis(request.getDiagnosis());
        }
        if (request.getFinalCost() != null) {
            repair.setFinalCost(request.getFinalCost());
        }
        if (newStatus == RepairStatus.COMPLETED) {
            repair.setCompletedAt(Instant.now());
        }

        RepairRequest saved = repairRequestRepository.save(repair);
        User changedBy = currentUser != null ? currentUser.getUser() : null;
        addHistory(saved, oldStatus, newStatus, request.getComment(), changedBy);
        return toResponseWithFetch(getRepair(saved.getId()));
    }

    @Transactional
    public RepairResponse assignMaster(Long id, AssignMasterRequest request) {
        RepairRequest repair = getRepair(id);
        User master = getMaster(request.getMasterId());
        repair.setMaster(master);
        RepairRequest saved = repairRequestRepository.save(repair);
        addHistory(saved, repair.getStatus(), repair.getStatus(),
                "Назначен мастер: " + master.getFullName(), null);
        return toResponseWithFetch(getRepair(saved.getId()));
    }

    @Transactional(readOnly = true)
    public List<ServiceHistoryResponse> getHistory(Long repairId) {
        if (!repairRequestRepository.existsById(repairId)) {
            throw new ResourceNotFoundException("Заявка не найдена: " + repairId);
        }
        return serviceHistoryRepository.findByRepairRequestIdOrderByCreatedAtDesc(repairId)
                .stream()
                .map(historyMapper::toResponse)
                .toList();
    }

    private void addHistory(RepairRequest repair, RepairStatus oldStatus, RepairStatus newStatus,
                            String comment, User changedBy) {
        ServiceHistory history = ServiceHistory.builder()
                .repairRequest(repair)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .comment(comment)
                .changedBy(changedBy)
                .build();
        serviceHistoryRepository.save(history);
    }

    private String generateRequestNumber() {
        int year = Year.now().getValue();
        String prefix = "SC-" + year + "-";
        int next = repairRequestRepository.findMaxRequestSuffix(prefix) + 1;
        String requestNumber;
        do {
            requestNumber = prefix + String.format("%05d", next);
            next++;
        } while (repairRequestRepository.existsByRequestNumber(requestNumber));
        return requestNumber;
    }

    private User getMaster(Long masterId) {
        User master = userRepository.findById(masterId)
                .orElseThrow(() -> new ResourceNotFoundException("Мастер не найден: " + masterId));
        if (master.getRole().getName() != RoleName.MASTER) {
            throw new BusinessException("Выбранный пользователь не является мастером");
        }
        return master;
    }

    private RepairRequest getRepair(Long id) {
        return repairRequestRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Заявка не найдена: " + id));
    }

    private RepairResponse toResponseWithFetch(RepairRequest repair) {
        repair.getClient().getFullName();
        repair.getDevice().getBrand();
        if (repair.getMaster() != null) {
            repair.getMaster().getFullName();
        }
        return repairMapper.toResponse(repair);
    }
}
