package ru.yandex.practicum.filmorate.dal;

import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;

@Repository
public class FilmRepository extends BaseRepository<Film> implements FilmStorage {
    private static final String UPDATE_FILM_QUERY = """
            UPDATE FILMS
            SET FILM_NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ?, RATING_ID = ?
            WHERE FILM_ID = ?
            """;

    private static final String DELETE_FILM_QUERY = "DELETE FROM FILMS WHERE FILM_ID = ?";

    private static final String QUERY_EXISTS_RATING = "SELECT COUNT(*) FROM RATING WHERE RATING_ID = ?";

    private static final String GET_COMMON_FILMS_QUERY = """
            SELECT f.*, r.RATING_NAME, COUNT(l.USER_ID) AS LIKES
            FROM FILMS f
            JOIN RATING r ON f.RATING_ID = r.RATING_ID
            JOIN LIKE_LIST l ON f.FILM_ID = l.FILM_ID
            JOIN LIKE_LIST l1 ON f.FILM_ID = l1.FILM_ID
            WHERE l.USER_ID = ? AND l1.USER_ID = ?
            GROUP BY f.FILM_ID, r.RATING_NAME, f.FILM_NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, f.RATING_ID
            ORDER BY LIKES DESC
            """;

    private final JdbcTemplate jdbc;

    @Autowired
    public FilmRepository(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
        this.jdbc = jdbc;
    }

    @Override
    public Film createFilm(Film film) {
        int id = insert("""
                        INSERT INTO FILMS (FILM_NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATING_ID)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId());
        if (id == 0) {
            throw new ValidationException("Ошибка создания фильма");
        }
        film.setId(id);
        return film;
    }

    @Override
    public Film updateFilm(Film filmUpdated) {
        int updatedRows = jdbc.update(UPDATE_FILM_QUERY,
                filmUpdated.getName(),
                filmUpdated.getDescription(),
                filmUpdated.getReleaseDate(),
                filmUpdated.getDuration(),
                filmUpdated.getMpa().getId(),
                filmUpdated.getId());
        if (updatedRows == 0) {
            throw new NotFoundException("Фильм с id " + filmUpdated.getId() + " не найден");
        }
        return filmUpdated;
    }

    @Override
    public List<Film> getFilms() {
        return findMany("SELECT * FROM FILMS");
    }

    @Override
    public Optional<Film> getFilmById(Integer id) {
        return findOne("""
                SELECT * FROM FILMS WHERE FILM_ID = ?
                """, id);
    }

    @Override
    public void deleteFilm(Integer filmId) {
        int deletedRows = jdbc.update(DELETE_FILM_QUERY, filmId);
        if (deletedRows == 0) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
    }

    @Override
    public Set<Film> getTopFilms() {
        return streamQuery("""
                SELECT F.FILM_ID, F.FILM_NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, F.RATING_ID, COUNT(L.FILM_ID) AS count
                FROM FILMS AS F
                JOIN LIKE_LIST AS L ON L.FILM_ID = F.FILM_ID
                GROUP BY F.FILM_ID
                ORDER BY count DESC
                """);
    }

    @Override
    public Collection<Film> getCommonFilms(int userId, int friendId) {
        Collection<Film> films = findMany(GET_COMMON_FILMS_QUERY, userId, friendId);
        return films;
    }

    @Override
    public Collection<Film> getFilmsByDirector(int directorId) {
        String sql = "SELECT * FROM FILMS WHERE FILM_ID IN (SELECT FILM_ID FROM DIRECTORS_SAVE WHERE DIRECTOR_ID = ?)";
        return findMany(sql, directorId);
    }

    public Collection<Film> getFilmsByTitle(String searchQuery) {
        String sql = """
        SELECT f.*, r.RATING_NAME
        FROM FILMS f
        LEFT JOIN RATING r ON f.RATING_ID = r.RATING_ID
        WHERE LOWER(f.FILM_NAME) LIKE ?
        """;
        String searchParam = "%" + searchQuery.toLowerCase() + "%";
        return findMany(sql, searchQuery);
    }

    public Collection<Film> getFilmsByDirectorName(String searchQuery) {
        String sql = "SELECT f.*, r.RATING_NAME " +
                "FROM FILMS f " +
                "LEFT JOIN RATING r ON f.RATING_ID = r.RATING_ID " +
                "WHERE f.FILM_ID IN (" +
                "  SELECT fd.FILM_ID " +
                "  FROM DIRECTORS_SAVE fd " +
                "  JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID " +
                "  WHERE LOWER(d.DIRECTOR_NAME) LIKE ?" +
                ")";
        String param = "%" + searchQuery.toLowerCase() + "%";
        return findMany(sql, param);
    }

    public boolean ratingExists(Integer ratingId) {
        Integer count = jdbc.queryForObject(QUERY_EXISTS_RATING, Integer.class, ratingId);
        return count > 0;
    }
}