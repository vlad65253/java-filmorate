package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    // Создание нового отзыва
    public Review createReview(Review review) {
        if (review.getContent() == null || review.getContent().isBlank()) {
            throw new ValidationException("Текст отзыва не может быть пустым.");
        }
        log.info("Попытка получить пользователя по id");
        userStorage.getUserById(review.getUserId());
        log.info("Попытка получить фильм по id");
        filmStorage.getFilmById(review.getFilmId());

        Review created = reviewStorage.createReview(review);
        log.info("Создан отзыв с id: {}", created.getReviewId());
        return created;
    }

    // Обновление существующего отзыва
    public Review updateReview(Review review) {
        reviewStorage.getReviewById(review.getReviewId())
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + review.getReviewId() + " не найден"));
        Review updated = reviewStorage.updateReview(review);
        log.info("Отзыв с id {} обновлён", review.getReviewId());
        return updated;
    }

    // Удаление отзыва по ID
    public void deleteReview(int reviewId) {
        if (!reviewStorage.deleteReview(reviewId)) {
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден");
        }
        log.info("Отзыв с id {} удалён", reviewId);
    }

    // Получение отзыва по ID
    public Review getReviewById(int reviewId) {
        return reviewStorage.getReviewById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + reviewId + " не найден"));
    }

    // Получение списка отзывов
    public List<Review> getReviews(Integer filmId, Integer count) {
        int limit = (count == null) ? 10 : count;
        List<Review> reviews = reviewStorage.getReviewsByFilmId(filmId, limit);
        log.info("Получено {} отзывов (filmId = {})", reviews.size(), filmId);
        return reviews;
    }

    // Методы для работы с лайками/дизлайками

    public void addLike(int reviewId, int userId) {
        getReviewById(reviewId);
        userStorage.getUserById(userId);
        reviewStorage.addLike(reviewId, userId);
        log.info("Пользователь {} поставил лайк отзыву {}", userId, reviewId);
    }

    public void addDislike(int reviewId, int userId) {
        getReviewById(reviewId);
        userStorage.getUserById(userId);
        reviewStorage.addDislike(reviewId, userId);
        log.info("Пользователь {} поставил дизлайк отзыву {}", userId, reviewId);
    }

    public void removeLike(int reviewId, int userId) {
        getReviewById(reviewId);
        userStorage.getUserById(userId);
        reviewStorage.removeLike(reviewId, userId);
        log.info("Пользователь {} удалил лайк у отзыва {}", userId, reviewId);
    }

    public void removeDislike(int reviewId, int userId) {
        getReviewById(reviewId);
        userStorage.getUserById(userId);
        reviewStorage.removeDislike(reviewId, userId);
        log.info("Пользователь {} удалил дизлайк у отзыва {}", userId, reviewId);
    }
}