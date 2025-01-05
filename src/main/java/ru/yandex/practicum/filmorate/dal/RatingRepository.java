package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;

@Repository
public class RatingRepository extends BaseRepository<Rating> {
    private static final String QUERY_FOR_ALL_RATING = "SELECT * FROM RATING";
    private static final String QUERY_FOR_BY_ID = "SELECT * FROM RATING WHERE RATING_ID = ?";

    public RatingRepository(JdbcTemplate jdbc, RowMapper<Rating> mapper) {
        super(jdbc, mapper);
    }

    public Collection<Rating> getAllRating() {
        return findMany(QUERY_FOR_ALL_RATING);
    }

    public Rating getRatingById(Integer id) {
        return findOne(QUERY_FOR_BY_ID, id);
    }
}
