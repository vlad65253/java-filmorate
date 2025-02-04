package ru.yandex.practicum.filmorate.model;

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
    private Integer userId;
    private Integer filmId;
    private String content;     // содержание отзыва
    private Boolean isPositive; // тип отзыва: позитивный (true) или негативный (false)
    private Integer useful;     // рейтинг полезности (изначально 0), считаем по оценкам ревью
}