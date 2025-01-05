package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.RatingRepository;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingService {
    @Autowired
    private final RatingRepository ratingRepository;

    public Collection<Rating> getAllRating() {
        return ratingRepository.getAllRating();
    }

    public Rating getRatingById(Integer id) {
        return ratingRepository.getRatingById(id);
    }
}
