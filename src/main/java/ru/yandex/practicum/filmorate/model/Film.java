package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.Duration;
import java.time.Instant;

/**
 * Film.
 */
@Data
public class Film {
    long id;
    String name;
    String description;
    Instant releaseDate;
    Duration duration;
}
