package ru.servicecenter.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecenter.server.domain.entity.Role;
import ru.servicecenter.server.domain.entity.User;
import ru.servicecenter.server.dto.user.UserRequest;
import ru.servicecenter.server.dto.user.UserResponse;
import ru.servicecenter.server.exception.BusinessException;
import ru.servicecenter.server.exception.ResourceNotFoundException;
import ru.servicecenter.server.mapper.UserMapper;
import ru.servicecenter.server.repository.RoleRepository;
import ru.servicecenter.server.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return userMapper.toResponse(getUser(id));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Пользователь с таким логином уже существует");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessException("Пароль обязателен при создании пользователя");
        }
        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .active(request.isActive())
                .role(getRole(request.getRole()))
                .build();
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = getUser(id);
        if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Пользователь с таким логином уже существует");
        }
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setActive(request.isActive());
        user.setRole(getRole(request.getRole()));
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Пользователь не найден: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findMasters() {
        return userRepository.findByRole_Name(ru.servicecenter.server.domain.enums.RoleName.MASTER)
                .stream()
                .filter(User::isActive)
                .map(userMapper::toResponse)
                .toList();
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + id));
    }

    private Role getRole(ru.servicecenter.server.domain.enums.RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Роль не найдена: " + roleName));
    }
}
