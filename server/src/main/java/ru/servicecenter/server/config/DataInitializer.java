package ru.servicecenter.server.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.servicecenter.server.service.bootstrap.DefaultUserProvisioner;

@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DefaultUserProvisioner defaultUserProvisioner;

    @Override
    public void run(String... args) {
        defaultUserProvisioner.provisionDefaultUsers();
    }
}
