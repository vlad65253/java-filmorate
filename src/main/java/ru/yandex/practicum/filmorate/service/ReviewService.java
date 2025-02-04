package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;

    public Review addReview(Review review) {
        // Гарантируем, что рейтинг полезности равен 0 при создании
        review.setUseful(0);
        return reviewStorage.createReview(review);
    }

    public Review updateReview(Review review) {
        return reviewStorage.updateReview(review);
    }

    public void deleteReview(int reviewId) {
        reviewStorage.deleteReview(reviewId);
    }

    public Review getReview(int reviewId) {
        return reviewStorage.getReview(reviewId);
    }

    /**
     * Получение списка отзывов.
     * Если filmId не указан, возвращаются все отзывы, с лимитом count (по умолчанию 10).
     * Отзывы сортируются по рейтингу полезности по убыванию.
     */
    public List<Review> getReviews(Integer filmId, Integer count) {
        int limit = (count != null) ? count : 10;
        return reviewStorage.getReviews(filmId, limit);
    }

    public void addLike(int reviewId, int userId) {
        reviewStorage.updateReviewUseful(reviewId, 1);
    }

    public void addDislike(int reviewId, int userId) {
        reviewStorage.updateReviewUseful(reviewId, -1);
    }

    public void deleteLike(int reviewId, int userId) {
        // Реализовано как обратное действие
        reviewStorage.updateReviewUseful(reviewId, -1);
    }

    public void deleteDislike(int reviewId, int userId) {
        reviewStorage.updateReviewUseful(reviewId, 1);
    }
}