package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.RatingStorage;

import java.util.List;

@Repository
public class RatingRepository extends BaseRepository<Rating> implements RatingStorage {

    public RatingRepository(JdbcTemplate jdbc, RowMapper<Rating> mapper) {
        super(jdbc, mapper);
    }

    public List<Rating> getAllRatings() {
        return findMany("""
                SELECT * FROM RATING
                """);
    }

    public Rating getRatingById(int id) {
        return findOne("""
                SELECT * FROM RATING WHERE RATING_ID = ?
                """, id).get();

    }

    public boolean ratingExists(Integer ratingId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM RATING WHERE RATING_ID = ?", Integer.class, ratingId);
        return count > 0;
    }

}