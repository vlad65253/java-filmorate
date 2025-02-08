package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.RatingRepository;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingRepository ratingRepository;

    public Collection<Rating> getAllRating() {
        Collection<Rating> ratings = ratingRepository.getAllRatings();
        log.debug("Получено рейтингов: {}", ratings.size());
        return ratings;
    }

    public Optional<Rating> getRatingById(Integer id) {
        log.debug("Получен рейтинг с id: {}", id);
        return ratingRepository.getRatingById(id);
    }
}