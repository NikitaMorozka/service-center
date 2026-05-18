package ru.servicecenter.server.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "device_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;
}
