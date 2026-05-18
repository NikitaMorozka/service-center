package ru.servicecenter.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.servicecenter.server.domain.entity.Role;
import ru.servicecenter.server.domain.enums.RoleName;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
