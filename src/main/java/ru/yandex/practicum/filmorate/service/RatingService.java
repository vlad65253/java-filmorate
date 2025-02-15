package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.RatingStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingStorage ratingStorage;

    public List<Rating> getAllRating() {
        List<Rating> ratings = ratingStorage.getAllRatings();
        log.debug("Получено рейтингов: {}", ratings.size());
        return ratings;
    }

    public Rating getRatingById(int id) {
        log.debug("Получен рейтинг с id: {}", id);
        return ratingStorage.getRatingById(id);
    }
}