package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {

    Review createReview(Review review);

    Review updateReview(Review review);

    Optional<Review> getReviewById(int reviewId);

    List<Review> getReviewsByFilmId(Integer filmId, int count);

    boolean deleteReview(int reviewId);

    void addLike(int reviewId, int userId);

    void addDislike(int reviewId, int userId);

    void removeLike(int reviewId, int userId);

    void removeDislike(int reviewId, int userId);
}