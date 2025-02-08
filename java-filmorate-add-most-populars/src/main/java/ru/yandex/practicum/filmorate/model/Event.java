package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Event {
    @Positive
    private long timestamp;
    @NonNull
    private Integer userId;
    @NotBlank
    private String eventType;
    @NotBlank
    private String operation;
    @NonNull
    private Integer eventId;
    @NonNull
    private Integer entityId;
}
