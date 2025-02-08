package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class GenreRepository extends BaseRepository<Genre> {
    private static final String GET_ALL_GENRE = "SELECT * FROM GENRE";
    private static final String GET_GENRE_BY_ID = "SELECT * FROM GENRE WHERE GENRE_ID = ?";
    private static final String ADD_GENRE_QUERY = "INSERT INTO FILMS_GENRE (FILM_ID, GENRE_ID) VALUES (?, ?)";
    private static final String DEL_GENRE_QUERY = "DELETE FROM FILMS_GENRE WHERE FILM_ID = ?";
    private static final String FIND_ALL_BY_FILMS = "SELECT fg.FILM_ID, g.GENRE_ID, g.GENRE_NAME FROM FILMS_GENRE fg LEFT JOIN GENRE g ON fg.GENRE_ID = g.GENRE_ID";
    private static final String FIND_BY_FILM_QUERY = "SELECT g.GENRE_ID, g.GENRE_NAME FROM GENRE g JOIN FILMS_GENRE fg ON g.GENRE_ID = fg.GENRE_ID WHERE fg.FILM_ID = ?";
    private static final String UPDATE_QUERY = "UPDATE GENRE SET GENRE_NAME = ? WHERE GENRE_ID = ?";

    public GenreRepository(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    public Collection<Genre> getAllGenres() {
        return findMany(GET_ALL_GENRE);
    }

    public Genre getGenreById(Integer id) {
        Genre genre = findOne(GET_GENRE_BY_ID, id);
        if (genre == null) {
            throw new NotFoundException("Жанр с id " + id + " не найден");
        }
        return genre;
    }

    public void addGenre(Film film, Genre genre) {
        jdbc.update(ADD_GENRE_QUERY, film.getId(), genre.getId());
    }

    public void addGenres(Film film, List<Genre> genres) {
        jdbc.batchUpdate(ADD_GENRE_QUERY, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, film.getId());
                ps.setInt(2, genres.get(i).getId());
            }
            @Override
            public int getBatchSize() {
                return genres.size();
            }
        });
    }

    public void updateGenre(Genre genre) {
        boolean updated = update(UPDATE_QUERY, genre.getName(), genre.getId());
        if (!updated) {
            throw new NotFoundException("Жанр с id " + genre.getId() + " не найден");
        }
    }

    public void delGenres(int filmId) {
        delete(DEL_GENRE_QUERY, filmId);
    }

    // Получаем жанры для конкретного фильма через streamQuery
    public Set<Genre> getGenresByFilm(int filmId) {
        return jdbc.queryForStream(FIND_BY_FILM_QUERY, new GenreRowMapper(), filmId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Map<Integer, Set<Genre>> getAllByFilms() {
        List<Map<String, Object>> rows = jdbc.queryForList(FIND_ALL_BY_FILMS);
        return rows.stream().collect(Collectors.groupingBy(
                row -> ((Number) row.get("FILM_ID")).intValue(),
                Collectors.mapping(
                        row -> Genre.builder()
                                .id((Integer) row.get("GENRE_ID"))
                                .name((String) row.get("GENRE_NAME"))
                                .build(),
                        Collectors.toCollection(LinkedHashSet::new)
                )
        ));
    }
}