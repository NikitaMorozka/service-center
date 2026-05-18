package ru.servicecenter.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.servicecenter.server.security.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class ServiceCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceCenterApplication.class, args);
    }
}
