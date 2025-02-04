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
        reviewStorage.deleteReview(reviewId);
    }

    public Review getReview(int reviewId) {
        return reviewStorage.getReview(reviewId);
    }

    /**
     * Получение списка отзывов.
     * Если filmId не указан, возвращаются все отзывы (с лимитом по умолчанию 10),
     * сортированные по рейтингу полезности (useful) по убыванию.
     */
    public List<Review> getReviews(Integer filmId, Integer count) {
        int limit = (count != null) ? count : 10;
        return reviewStorage.getReviews(filmId, limit);
    }

    /**
     * Пользователь ставит лайк отзыву — увеличивается рейтинг useful на 1
     */
    public void addLike(int reviewId, int userId) {
        reviewStorage.updateReviewUseful(reviewId, 1);
    }

    /**
     * Пользователь ставит дизлайк отзыву
     * Если пользователь ранее поставил лайк, то переключение на дизлайк должно уменьшить рейтинг на 2
     * Здесь реализую именно такое переключение, поэтому вычитаем 2
     */
    public void addDislike(int reviewId, int userId) {
        reviewStorage.updateReviewUseful(reviewId, -2);
    }

    /**
     * Пользователь удаляет свой лайк отзыва — уменьшается рейтинг useful на 1
     */
    public void deleteLike(int reviewId, int userId) {
        reviewStorage.updateReviewUseful(reviewId, -1);
    }

    /**
     * Пользователь удаляет свой дизлайк отзыва — увеличивается рейтинг useful на 2
     */
    public void deleteDislike(int reviewId, int userId) {
        reviewStorage.updateReviewUseful(reviewId, 2);
    }
}