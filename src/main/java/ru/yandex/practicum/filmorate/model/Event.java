package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Event {
    @Positive
    private long timestamp;
    @NotNull
    private Integer userId;
    @NotBlank
    private String eventType;
    @NotBlank
    private String operation;
    @NotNull
    private Integer eventId;
    @NotNull
    private Integer entityId;
}
