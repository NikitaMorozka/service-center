package ru.servicecenter.server.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.servicecenter.server.domain.entity.Brand;
import ru.servicecenter.server.domain.entity.DeviceType;
import ru.servicecenter.server.domain.entity.Role;
import ru.servicecenter.server.domain.entity.User;
import ru.servicecenter.server.domain.enums.RoleName;
import ru.servicecenter.server.repository.BrandRepository;
import ru.servicecenter.server.repository.DeviceTypeRepository;
import ru.servicecenter.server.repository.RoleRepository;
import ru.servicecenter.server.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final DeviceTypeRepository deviceTypeRepository;
    private final BrandRepository brandRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String[] DEFAULT_DEVICE_TYPES = {
            "Телефон", "Телевизор", "Стиральная машина", "Ноутбук", "Планшет", "Холодильник"
    };

    private static final String[] DEFAULT_BRANDS = {
            "Samsung", "LG", "Realme", "Apple", "Xiaomi", "Sony", "Bosch", "Philips"
    };

    @Override
    public void run(String... args) {
        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName).orElseGet(() ->
                    roleRepository.save(Role.builder().name(roleName).build()));
        }

        for (String typeName : DEFAULT_DEVICE_TYPES) {
            if (!deviceTypeRepository.existsByNameIgnoreCase(typeName)) {
                deviceTypeRepository.save(DeviceType.builder().name(typeName).build());
            }
        }
        for (String brandName : DEFAULT_BRANDS) {
            if (!brandRepository.existsByNameIgnoreCase(brandName)) {
                brandRepository.save(Brand.builder().name(brandName).build());
            }
        }

        if (userRepository.findByUsername("admin").isEmpty()) {
            Role adminRole = roleRepository.findByName(RoleName.ADMIN).orElseThrow();
            userRepository.save(User.builder()
                    .username("admin")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .fullName("Администратор системы")
                    .email("admin@servicecenter.local")
                    .active(true)
                    .role(adminRole)
                    .build());
            log.info("Создан пользователь admin / admin123");
        }

        if (userRepository.findByUsername("manager").isEmpty()) {
            Role managerRole = roleRepository.findByName(RoleName.MANAGER).orElseThrow();
            userRepository.save(User.builder()
                    .username("manager")
                    .passwordHash(passwordEncoder.encode("manager123"))
                    .fullName("Иван Менеджеров")
                    .email("manager@servicecenter.local")
                    .active(true)
                    .role(managerRole)
                    .build());
            log.info("Создан пользователь manager / manager123");
        }

        if (userRepository.findByUsername("master").isEmpty()) {
            Role masterRole = roleRepository.findByName(RoleName.MASTER).orElseThrow();
            userRepository.save(User.builder()
                    .username("master")
                    .passwordHash(passwordEncoder.encode("master123"))
                    .fullName("Пётр Мастеров")
                    .email("master@servicecenter.local")
                    .active(true)
                    .role(masterRole)
                    .build());
            log.info("Создан пользователь master / master123");
        }
    }
}
