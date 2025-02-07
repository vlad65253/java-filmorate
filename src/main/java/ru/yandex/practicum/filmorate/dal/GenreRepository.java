package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;

@Repository
public class GenreRepository extends BaseRepository<Genre> {
    private static final String GET_ALL_GERNE = "SELECT * FROM GENRE";
    private static final String GET_GERNE_BY_ID = "SELECT * FROM GENRE " +
            "WHERE GENRE_ID = ?";
    private static final String ADD_GENRE_QUERY = "INSERT INTO FILMS_GENRE (FILM_ID, GENRE_ID) VALUES (?, ?)";

    private static final String DEL_GENRE_QUERY = "DELETE FROM FILMS_GENRE WHERE FILM_ID = ?";
    private static final String FIND_ALL_BY_FILMS = """
            SELECT fg.FILM_ID, g.*
            FROM FILMS_GENRE fg
            LEFT JOIN GENRE g ON fg.GENRE_ID = g.GENRE_ID
            """;
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM GENRE WHERE GENRE_ID IN " +
            "(SELECT GENRE_ID FROM FILMS_GENRE WHERE FILM_ID = ?)";
    private static final String UPDATE_QUERY = "UPDATE GENRE SET GENRE_NAME = ? WHERE GENRE_ID = ?";



    public GenreRepository(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    public Collection<Genre> getAllGenres() {
        return findMany(GET_ALL_GERNE);
    }

    public Genre getGenreById(Integer id) {
        return findOne(GET_GERNE_BY_ID, id);
    }

    public void addGenre(Film film, Genre genre) {
        jdbc.update(ADD_GENRE_QUERY, film.getId(), genre.getId());
    }
    public List<Genre> getGenres(Film film){
        return jdbc.query(FIND_BY_ID_QUERY, new GenreRowMapper(), film.getId());
    }

    public void delGenres(long id) {
        delete(DEL_GENRE_QUERY, id);
    }
    public Genre update(Genre genre) {
        update(UPDATE_QUERY, genre.getName(), genre.getId());
        return genre;
    }

    public Map<Integer, Set<Genre>> getAllByFilms() {
        List<Map<String, Object>> rows = jdbc.queryForList(FIND_ALL_BY_FILMS);
        return rows.stream().collect(groupingBy(row -> ((Number) row.get("FILM_ID"))
                                .intValue(),
                        mapping(row -> Genre.builder()
                                .id((Integer) row.get("GENRE_ID"))
                                .name((String) row.get("GENRE_NAME"))
                                .build(), Collectors.toCollection(LinkedHashSet::new))
                )
        );
    }

    public void addGenres(Film film, List<Genre> genres) {
        jdbc.batchUpdate(ADD_GENRE_QUERY, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, film.getId());
                ps.setLong(2, genres.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return genres.size();
            }
        });
    }
}
