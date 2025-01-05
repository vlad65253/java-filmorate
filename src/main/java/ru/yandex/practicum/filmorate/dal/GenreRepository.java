package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Repository
public class GenreRepository extends BaseRepository<Genre> {
    private static final String GET_ALL_GERNE = "SELECT * FROM GENRE";
    private static final String GET_GERNE_BY_ID = "SELECT * FROM GENRE " +
            "WHERE GENRE_ID = ?";
    private static final String ADD_GENRE_QUERY = "INSERT INTO FILMS_GENRE (FILM_ID, GENRE_ID) VALUES (?, ?)";
    private static final String DEL_GENRE_QUERY = "DELETE FROM FILMS_GENRE WHERE FILM_ID = ?";


    public GenreRepository(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    public Collection<Genre> getAllGenres() {
        return findMany(GET_ALL_GERNE);
    }

    public Genre getGenreById(Integer id) {
        return findOne(GET_GERNE_BY_ID, id);
    }

    public void addGenres(Integer filmId, List<Integer> genresId) {
        batchUpdateBase(ADD_GENRE_QUERY, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, filmId);
                ps.setLong(2, genresId.get(i));
            }

            @Override
            public int getBatchSize() {
                return genresId.size();
            }
        });
    }

    public void delGenres(long id) {
        delete(DEL_GENRE_QUERY, id);
    }
}
