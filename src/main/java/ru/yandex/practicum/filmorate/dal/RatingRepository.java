package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.RatingStorage;

import java.util.Collection;
import java.util.Optional;

@Repository
public class RatingRepository extends BaseRepository<Rating> implements RatingStorage {
    public RatingRepository(JdbcTemplate jdbc, RowMapper<Rating> mapper) {
        super(jdbc, mapper);
    }

    public Collection<Rating> getAllRatings() {
        return findMany("""
                SELECT * FROM RATING
                """);
    }

    public Optional<Rating> getRatingById(Integer id) {
        return findOne("""
                SELECT * FROM RATING WHERE RATING_ID = ?
                """, id);

    }


}