package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Review {
    private Integer reviewId;

    @NotNull(message = "User ID не должен быть null")
    private Integer userId;

    @NotNull(message = "Film ID не должен быть null")
    private Integer filmId;

    @NotBlank(message = "Содержание отзыва не может быть пустым")
    @Size(max = 500, message = "Содержание отзыва не должно превышать 500 символов")
    private String content;

    @NotNull(message = "Поле isPositive не должно быть null")
    private Boolean isPositive; // Тип отзыва: позитивный (true) или негативный (false)

    private Integer useful;     // Рейтинг полезности (изначально 0)
}