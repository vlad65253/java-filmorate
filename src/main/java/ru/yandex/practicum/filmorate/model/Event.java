package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    private Integer eventId;
    @Positive
    private Timestamp eventTimestamp;
    @NotBlank
    private Integer userId;
    @NotBlank
    private String eventType;
    @NotBlank
    private String operation;
    @NotBlank
    private Integer entityId;
}
