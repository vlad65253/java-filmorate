package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;

    public Review addReview(Review review) {
        review.setUseful(0);
        try {
            return reviewStorage.createReview(review);
        } catch (DataIntegrityViolationException ex) {
            throw new NotFoundException("Пользователь или фильм не найдены");
        }
    }

    public Review updateReview(Review review) {
        return reviewStorage.updateReview(review);
    }

    public void deleteReview(int reviewId) {
        if (reviewStorage.getReview(reviewId) == null) {
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден");
        }
        int userId = reviewStorage.getReview(reviewId).getUserId();
        reviewStorage.deleteReview(userId, reviewId);
    }

    public Review getReview(int reviewId) {
        return reviewStorage.getReview(reviewId);
    }

    public List<Review> getReviews(Integer filmId, Integer count) {
        int limit = (count != null) ? count : 10;
        return reviewStorage.getReviews(filmId, limit);
    }

    public void addLike(int reviewId, int userId) {
        reviewStorage.updateReviewUseful(reviewId, 1);
    }

    public void addDislike(int reviewId, int userId) {
        reviewStorage.updateReviewUseful(reviewId, -2);
    }

    public void deleteLike(int reviewId, int userId) {
        reviewStorage.updateReviewUseful(reviewId, -1);
    }

    public void deleteDislike(int reviewId, int userId) {
        reviewStorage.updateReviewUseful(reviewId, 2);
    }
}