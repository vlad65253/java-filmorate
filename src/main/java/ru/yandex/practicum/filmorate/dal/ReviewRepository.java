package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.ReviewRowMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

@Repository
public class ReviewRepository extends BaseRepository<Review> implements ReviewStorage {
    private static final String SQL_INSERT_REVIEW =
            "INSERT INTO REVIEWS (USER_ID, FILM_ID, CONTENT, IS_POSITIVE, USEFUL) " +
                    "VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE_REVIEW =
            "UPDATE REVIEWS SET CONTENT = ?, IS_POSITIVE = ? WHERE REVIEW_ID = ?";
    private static final String SQL_DELETE_REVIEW =
            "DELETE FROM REVIEWS WHERE REVIEW_ID = ?";
    private static final String SQL_SELECT_REVIEW =
            "SELECT * FROM REVIEWS WHERE REVIEW_ID = ?";
    private static final String SQL_SELECT_REVIEWS_BY_FILM =
            "SELECT * FROM REVIEWS WHERE FILM_ID = ? ORDER BY USEFUL DESC LIMIT ?";
    private static final String SQL_SELECT_ALL_REVIEWS =
            "SELECT * FROM REVIEWS ORDER BY USEFUL DESC LIMIT ?";
    private static final String SQL_UPDATE_REVIEW_USEFUL =
            "UPDATE REVIEWS SET USEFUL = USEFUL + ? WHERE REVIEW_ID = ?";

    public ReviewRepository(JdbcTemplate jdbc, ReviewRowMapper reviewRowMapper) {
        super(jdbc, reviewRowMapper);
    }

    @Override
    public Review createReview(Review review) {
        // Если полезность не задана, ставим 0
        int useful = review.getUseful() != null ? review.getUseful() : 0;
        Integer id = insert(SQL_INSERT_REVIEW,
                review.getUserId(),
                review.getFilmId(),
                review.getContent(),
                review.getIsPositive(),
                useful);
        review.setReviewId(id);
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        boolean updated = update(SQL_UPDATE_REVIEW,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());
        if (!updated) {
            throw new NotFoundException("Отзыв с id " + review.getReviewId() + " не найден");
        }
        return review;
    }

    @Override
    public void deleteReview(int reviewId) {
        boolean deleted = delete(SQL_DELETE_REVIEW, reviewId);
        if (!deleted) {
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден");
        }
    }

    @Override
    public Review getReview(int reviewId) {
        return findOne(SQL_SELECT_REVIEW, reviewId);
    }

    @Override
    public List<Review> getReviews(Integer filmId, int count) {
        if (filmId != null) {
            return findMany(SQL_SELECT_REVIEWS_BY_FILM, filmId, count);
        } else {
            return findMany(SQL_SELECT_ALL_REVIEWS, count);
        }
    }

    @Override
    public void updateReviewUseful(int reviewId, int delta) {
        boolean updated = update(SQL_UPDATE_REVIEW_USEFUL, delta, reviewId);
        if (!updated) {
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден");
        }
    }
}