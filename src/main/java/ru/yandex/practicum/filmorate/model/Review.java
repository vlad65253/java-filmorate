package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review {
    private Integer reviews_id;
    @NotBlank
    private String content;
    @NotBlank
    private Boolean isPositive;
    @NotBlank
    private Integer userId;
    @NotBlank
    private Integer filmId;
    private Integer useful;
}
