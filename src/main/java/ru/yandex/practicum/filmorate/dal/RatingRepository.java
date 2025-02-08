package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.RatingStorage;

import java.util.Collection;

@Repository
public class RatingRepository extends BaseRepository<Rating> implements RatingStorage {
    private static final String QUERY_FOR_ALL_RATING = "SELECT * FROM RATING";
    private static final String QUERY_FOR_BY_ID = "SELECT * FROM RATING WHERE RATING_ID = ?";

    public RatingRepository(JdbcTemplate jdbc, RowMapper<Rating> mapper) {
        super(jdbc, mapper);
    }

    public Collection<Rating> getAllRatings() {
        return findMany(QUERY_FOR_ALL_RATING);
    }

    public Rating getRatingById(Integer id) {
        Rating rating = findOne(QUERY_FOR_BY_ID, id);
        if (rating == null) {
            throw new NotFoundException("Рейтинг с id " + id + " не найден");
        }
        return rating;
    }
}