package ru.servicecenter.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.servicecenter.server.domain.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    List<User> findByRole_Name(ru.servicecenter.server.domain.enums.RoleName roleName);
}
