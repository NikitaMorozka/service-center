package ru.servicecenter.server.specification;

import org.springframework.data.jpa.domain.Specification;
import ru.servicecenter.server.domain.entity.RepairRequest;
import ru.servicecenter.server.domain.enums.RepairStatus;

import java.util.List;

public final class RepairSpecifications {

    private static final List<RepairStatus> ARCHIVED_STATUSES = List.of(
            RepairStatus.COMPLETED, RepairStatus.CANCELED);

    private RepairSpecifications() {
    }

    public static Specification<RepairRequest> hasStatus(RepairStatus status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    /** true — только завершённые/отменённые; false — только активные заявки */
    public static Specification<RepairRequest> isArchived(Boolean archived) {
        return (root, query, cb) -> {
            if (archived == null) {
                return cb.conjunction();
            }
            if (archived) {
                return root.get("status").in(ARCHIVED_STATUSES);
            }
            return cb.not(root.get("status").in(ARCHIVED_STATUSES));
        };
    }

    public static Specification<RepairRequest> hasMasterId(Long masterId) {
        return (root, query, cb) ->
                masterId == null ? cb.conjunction() : cb.equal(root.get("master").get("id"), masterId);
    }

    public static Specification<RepairRequest> search(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + q.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("requestNumber")), pattern),
                    cb.like(cb.lower(root.get("client").get("fullName")), pattern),
                    cb.like(cb.lower(root.get("device").get("brand")), pattern),
                    cb.like(cb.lower(root.get("device").get("model")), pattern)
            );
        };
    }
}
