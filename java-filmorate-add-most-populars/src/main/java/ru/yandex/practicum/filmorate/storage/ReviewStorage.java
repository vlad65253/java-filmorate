package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {

    Review createReview(Review review);

    Review updateReview(Review review);

    void deleteReview(int userId, int reviewId);

    Review getReview(int reviewId);

    List<Review> getReviews(Integer filmId, int count);

    void updateReviewUseful(int reviewId, int delta);
}