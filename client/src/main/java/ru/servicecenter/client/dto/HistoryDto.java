package ru.servicecenter.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoryDto {
    private String oldStatus;
    private String newStatus;
    private String changedByName;
    private String comment;
    private Instant createdAt;
}
