package ru.servicecenter.server.specification;

import org.springframework.data.jpa.domain.Specification;
import ru.servicecenter.server.domain.entity.Client;

public final class ClientSpecifications {

    private ClientSpecifications() {
    }

    public static Specification<Client> search(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + q.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("fullName")), pattern),
                    cb.like(cb.lower(root.get("phone")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern)
            );
        };
    }
}
