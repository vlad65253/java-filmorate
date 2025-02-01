package ru.yandex.practicum.filmorate.dal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.ResultSet;
import java.util.*;

@Repository
public class FilmRepository extends BaseRepository<Film> implements FilmStorage {
    private static final String CREATE_FILM_QUERY = "INSERT INTO FILMS (FILM_NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATING_ID) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM_QUERY = "UPDATE FILMS SET FILM_NAME = ?, DESCRIPTION = ?, " +
            "RELEASE_DATE = ?, DURATION = ?, RATING_ID = ? WHERE FILM_ID = ?";
    private static final String GET_ALL_FILMS_QUERY = "SELECT * FROM FILMS f, " +
            "RATING r WHERE f.RATING_ID = r.RATING_ID";
    private static final String GET_FILM_QUERY = "SELECT * FROM FILMS f, RATING r " +
            "WHERE f.RATING_ID = r.RATING_ID AND f.FILM_ID = ?";
    private static final String GET_ALL_GENERES_FILMS = "SELECT * " +
            "FROM FILMS_GENRE fg, " +
            "GENRE g WHERE fg.GENRE_ID = g.GENRE_ID";
    private static final String GET_GENRES_BY_FILM = "SELECT * FROM GENRE g, FILMS_GENRE fg " +
            "WHERE g.GENRE_ID = fg.GENRE_ID AND fg.FILM_ID = ?";
    private static final String DELETE_FILM_QUERY = "DELETE FROM FILMS WHERE FILM_ID = ?";
    private static final String QUERY_TOP_FILMS = "SELECT * FROM FILMS f LEFT JOIN RATING m " +
            "ON f.RATING_ID = m.RATING_ID LEFT JOIN (SELECT FILM_ID, COUNT(FILM_ID) AS LIKES FROM LIKE_LIST " +
            "GROUP BY FILM_ID) fl ON f.FILM_ID = fl.FILM_ID ORDER BY LIKES DESC LIMIT ?";
    private static final String FIND_FILMS_BY_DIRECTOR_ID_ORDER_BY_RELEASE_DATE_QUERY = """
            SELECT f.*, r.RATING_NAME AS mpa_name
            FROM FILMS f
            JOIN FILM_DIRECTORS fd ON f.FILM_ID = fd.FILM_ID
            JOIN RATING r ON f.RATING_ID = r.RATING_ID
            WHERE fd.DIRECTOR_ID = ?
            ORDER BY f.RELEASE_DATE
            """;
    private static final String FIND_FILMS_BY_DIRECTOR_ID_ORDER_BY_LIKES_QUERY = """
            SELECT f.*, r.RATING_NAME AS mpa_name, COUNT(l.FILM_ID) AS COUNT_LIKES
            FROM FILMS f
            JOIN FILM_DIRECTORS fd ON f.FILM_ID = fd.FILM_ID
            JOIN RATING r ON f.RATING_ID = r.RATING_ID
            JOIN LIKE_LIST l ON f.FILM_ID = l.FILM_ID
            WHERE fd.DIRECTOR_ID = ?
            GROUP BY l.FILM_ID
            ORDER BY COUNT_LIKES DESC
            """;
    private static final String QUERY_EXISTS_RATING = "SELECT COUNT(*) FROM RATING WHERE RATING_ID = ?";
    private static final String QUERY_EXISTS_GENRE = "SELECT COUNT(*) FROM GENRE WHERE GENRE_ID = ?";

    @Autowired
    public FilmRepository(JdbcTemplate jdbs, RowMapper<Film> mapper) {
        super(jdbs, mapper);
    }

    @Override
    public Film createFilm(Film film) {
        int id = insert(CREATE_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId());
        film.setId(id);
        return film;
    }

    @Override
    public Film updateFilm(Film filmUpdated) {
        update(UPDATE_FILM_QUERY,
                filmUpdated.getName(),
                filmUpdated.getDescription(),
                filmUpdated.getReleaseDate(),
                filmUpdated.getDuration(),
                filmUpdated.getMpa().getId(),
                filmUpdated.getId());
        return filmUpdated;

    }

    @Override
    public Collection<Film> getFilms() {
        Collection<Film> films = findMany(GET_ALL_FILMS_QUERY);
        Map<Integer, Set<Genre>> genres = getAllGenres();
        for (Film film : films) {
            if (genres.containsKey(film.getId())) {
                film.setGenres(genres.get(film.getId()));
            }
        }
        return films;
    }

    @Override
    public Collection<Film> getTopFilms(Integer count) {
        Collection<Film> films = findMany(QUERY_TOP_FILMS, count);
        Map<Integer, Set<Genre>> genres = getAllGenres();
        for (Film film : films) {
            if (genres.containsKey(film.getId())) {
                film.setGenres(genres.get(film.getId()));
            }
        }
        return films;
    }

    @Override
    public Film getFilm(Integer id) {
        Film film = findOne(GET_FILM_QUERY, id);
        film.setGenres(getGenresByFilm(id));
        return film;
    }

    @Override
    public void deleteFilm(Integer id) {
        delete(DELETE_FILM_QUERY, id);
    }

    private Map<Integer, Set<Genre>> getAllGenres() {
        Map<Integer, Set<Genre>> genres = new HashMap<>();
        return jdbc.query(GET_ALL_GENERES_FILMS, (ResultSet rs) -> {
            while (rs.next()) {
                Integer filmId = rs.getInt("FILM_ID");
                Integer genreId = rs.getInt("GENRE_ID");
                String genreName = rs.getString("GENRE_NAME");
                genres.computeIfAbsent(filmId, k -> new HashSet<>()).add(new Genre(genreId, genreName));
            }
            return genres;
        });
    }
    @Override
    public Collection<Film> getByDirectorId(int directorId, String sortBy) {
        String query = "year".equals(sortBy)
                ? FIND_FILMS_BY_DIRECTOR_ID_ORDER_BY_RELEASE_DATE_QUERY
                : FIND_FILMS_BY_DIRECTOR_ID_ORDER_BY_LIKES_QUERY;

        return findMany(query, directorId);
    }
    private Set<Genre> getGenresByFilm(long filmId) {
        return jdbc.query(GET_GENRES_BY_FILM, (ResultSet rs) -> {
            Set<Genre> genres = new HashSet<>();
            while (rs.next()) {
                int genreId = rs.getInt("GENRE_ID");
                String genreName = rs.getString("GENRE_NAME");
                genres.add(new Genre(genreId, genreName));
            }
            return genres;
        }, filmId);
    }
    public boolean ratingExists(Integer ratingId) {
        Integer count = jdbc.queryForObject(
                QUERY_EXISTS_RATING,
                Integer.class,
                ratingId
        );
        return count != null && count > 0;
    }
    public boolean genreTry(Integer genreId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM GENRE WHERE GENRE_ID = ?",
                Integer.class,
                genreId
        );
        return count != null && count > 0;
    }


}
