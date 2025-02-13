package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collections;
import java.util.List;

@Repository
public class FilmRepository extends BaseRepository<Film> implements FilmStorage {

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
            throw new NotFoundException("Ошибка создания фильма");
        }
        film.setId(id);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        update("""
                UPDATE FILMS
                SET FILM_NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ?, RATING_ID = ?
                WHERE FILM_ID = ?
                """,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        return film;
    }

    @Override
    public List<Film> getFilms() {
        String sql = """
                SELECT f.*, r.RATING_NAME as mpa_name
                FROM FILMS f
                LEFT JOIN RATING r ON f.RATING_ID = r.RATING_ID
                """;
        return findMany(sql);
    }

    @Override
    public Film getFilmById(int id) {
        String sql = """
                SELECT f.*, r.RATING_NAME as mpa_name
                FROM FILMS f
                LEFT JOIN RATING r ON f.RATING_ID = r.RATING_ID
                WHERE f.FILM_ID = ?
                """;
        return findOne(sql, id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    @Override
    public void deleteFilm(int filmId) {
        if (!update("DELETE FROM FILMS WHERE FILM_ID = ?", filmId)) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
    }

    @Override
    public List<Film> getFilmsByDirector(int directorId) {
        String sql = """
                SELECT f.*, r.RATING_NAME as mpa_name
                FROM FILMS f
                LEFT JOIN RATING r ON f.RATING_ID = r.RATING_ID
                WHERE f.FILM_ID IN (
                    SELECT FILM_ID FROM DIRECTORS_SAVE WHERE DIRECTOR_ID = ?
                )
                """;
        return findMany(sql, directorId);
    }

    @Override
    public List<Film> getFilmsByTitle(String searchQuery) {
        String sql = """
                SELECT f.*, r.RATING_NAME as mpa_name
                FROM FILMS f
                LEFT JOIN RATING r ON f.RATING_ID = r.RATING_ID
                WHERE LOWER(f.FILM_NAME) LIKE ?
                """;
        String searchParam = "%" + searchQuery.toLowerCase() + "%";
        return findMany(sql, searchParam);
    }

    @Override
    public List<Film> getFilmsByDirectorName(String searchQuery) {
        String sql = """
                SELECT f.*, r.RATING_NAME as mpa_name
                FROM FILMS f
                LEFT JOIN RATING r ON f.RATING_ID = r.RATING_ID
                WHERE f.FILM_ID IN (
                    SELECT fd.FILM_ID FROM DIRECTORS_SAVE fd
                    JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID
                    WHERE LOWER(d.DIRECTOR_NAME) LIKE ?
                )
                """;
        String searchParam = "%" + searchQuery.toLowerCase() + "%";
        return findMany(sql, searchParam);
    }

    @Override
    public List<Film> getRecommendations(long userId) {
        // Находим ID похожего пользователя (у которого максимальное пересечение лайков)
        String queryIntersection = """
                SELECT ll.USER_ID
                FROM LIKE_LIST ll
                WHERE ll.FILM_ID IN (SELECT FILM_ID FROM LIKE_LIST WHERE USER_ID = ?)
                  AND ll.USER_ID <> ?
                GROUP BY ll.USER_ID
                ORDER BY COUNT(ll.FILM_ID) DESC
                LIMIT ?
                """;
        List<Long> similarUserIds = jdbc.queryForList(queryIntersection, Long.class, userId, userId, 1);
        if (similarUserIds.isEmpty()) {
            return Collections.emptyList();
        }

        String queryFilmsByUser = """
                SELECT f.*, r.RATING_NAME as mpa_name
                FROM FILMS f
                LEFT JOIN RATING r ON f.RATING_ID = r.RATING_ID
                LEFT JOIN LIKE_LIST ll ON f.FILM_ID = ll.FILM_ID
                WHERE ll.USER_ID = ?
                """;
        List<Film> filmsLikedByUser = findMany(queryFilmsByUser, userId);

        String inSql = String.join(",", Collections.nCopies(similarUserIds.size(), "?"));
        String queryFilmsBySimilar = String.format("""
                SELECT f.*, r.RATING_NAME as mpa_name
                FROM FILMS f
                LEFT JOIN RATING r ON f.RATING_ID = r.RATING_ID
                LEFT JOIN LIKE_LIST ll ON f.FILM_ID = ll.FILM_ID
                WHERE ll.USER_ID IN (%s)
                """, inSql);
        List<Film> filmsLikedBySimilar = findMany(queryFilmsBySimilar, similarUserIds.toArray());

        // Исключаем из списка фильмы, которые уже лайкнул текущий пользователь
        filmsLikedBySimilar.removeAll(filmsLikedByUser);

        return filmsLikedBySimilar;
    }
}