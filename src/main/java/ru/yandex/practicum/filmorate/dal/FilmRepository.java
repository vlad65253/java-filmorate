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

    @Autowired
    public FilmRepository(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
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
        int updatedRows = jdbc.update("""
                        UPDATE FILMS
                        SET FILM_NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ?, RATING_ID = ?
                        WHERE FILM_ID = ?
                        """,
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
    public Film getFilmById(Integer id) {
        return findOne("""
                SELECT * FROM FILMS WHERE FILM_ID = ?
                """, id).get();
    }

    @Override
    public void deleteFilm(Integer filmId) {
        int deletedRows = jdbc.update("DELETE FROM FILMS WHERE FILM_ID = ?", filmId);
        if (deletedRows == 0) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
    }

    @Override
    public List<Film> getFilmsByDirector(int directorId) {
        String sql = "SELECT * FROM FILMS WHERE FILM_ID IN (SELECT FILM_ID FROM DIRECTORS_SAVE WHERE DIRECTOR_ID = ?)";
        return findMany(sql, directorId);
    }

    public List<Film> getFilmsByTitle(String searchQuery) {
        String sql = """
                SELECT f.*, r.RATING_NAME
                FROM FILMS f
                LEFT JOIN RATING r ON f.RATING_ID = r.RATING_ID
                WHERE LOWER(f.FILM_NAME) LIKE ?
                """;
        String searchParam = "%" + searchQuery.toLowerCase() + "%";
        return findMany(sql, searchParam);
    }

    public List<Film> getFilmsByDirectorName(String searchQuery) {
        String sql = "SELECT f.*, r.RATING_NAME " +
                "FROM FILMS f " +
                "LEFT JOIN RATING r ON f.RATING_ID = r.RATING_ID " +
                "WHERE f.FILM_ID IN (" +
                "  SELECT fd.FILM_ID " +
                "  FROM DIRECTORS_SAVE fd " +
                "  JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID " +
                "  WHERE LOWER(d.DIRECTOR_NAME) LIKE ?" +
                ")";
        String searchParam = "%" + searchQuery.toLowerCase() + "%";
        return findMany(sql, searchParam);
    }

    public boolean ratingExists(Integer ratingId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM RATING WHERE RATING_ID = ?", Integer.class, ratingId);
        return count > 0;
    }
}