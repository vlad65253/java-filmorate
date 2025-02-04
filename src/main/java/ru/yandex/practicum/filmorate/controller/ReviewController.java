package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    /**
     * POST /reviews
     * При создании рейтинг отзыва всегда равен 0.
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Review addReview(@Valid @RequestBody Review review) {
        return reviewService.addReview(review);
    }

    /**
     * PUT /reviews
     */
    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        return reviewService.updateReview(review);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable int id) {
        reviewService.deleteReview(id);
    }

    /**
     * GET /reviews/{id}
     */
    @GetMapping("/{id}")
    public Review getReview(@PathVariable int id) {
        return reviewService.getReview(id);
    }

    /**
     * GET /reviews?filmId={filmId}&count={count}
     * Получение списка отзывов.
     * Если filmId не указан, возвращаются все отзывы (с лимитом по умолчанию 10).
     * Отзывы сортируются по рейтингу полезности.
     */
    @GetMapping
    public List<Review> getReviews(@RequestParam(required = false) Integer filmId,
                                   @RequestParam(required = false) Integer count) {
        return reviewService.getReviews(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable int id, @PathVariable int userId) {
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable int id, @PathVariable int userId) {
        reviewService.deleteLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable int id, @PathVariable int userId) {
        reviewService.deleteDislike(id, userId);
    }
}