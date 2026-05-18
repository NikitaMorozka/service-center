package ru.servicecenter.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientDto {
    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private String notes;
}
