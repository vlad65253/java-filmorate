package ru.yandex.practicum.filmorate.dal;

import jakarta.validation.ValidationException;
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
    private static final String GET_COMMON_FILMS_QUERY = """
            SELECT f.*, r.RATING_NAME as mpa_name, COUNT(l.USER_ID) as LIKES
            FROM FILMS f
            JOIN RATING r ON f.RATING_ID = r.RATING_ID
            JOIN LIKE_LIST l ON f.FILM_ID = l.FILM_ID
            JOIN LIKE_LIST l1 ON f.FILM_ID = l1.FILM_ID
            WHERE l.USER_ID = ? AND l1.USER_ID = ?
            GROUP BY f.FILM_ID, r.RATING_NAME, f.FILM_NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, f.RATING_ID
            ORDER BY LIKES DESC
            """;
    private static final String FIND_BY_NAME_QUERY = "SELECT f.*, r.RATING_NAME mpa_name FROM FILMS f " +
            "LEFT JOIN RATING r ON f.RATING_ID = r.RATING_ID WHERE LOWER(f.FILM_NAME) like LOWER(?)";
    private static final String FIND_BY_DIRECTOR_NAME_QUERY = "SELECT f.*, r.RATING_NAME mpa_name FROM FILMS f " +
            "LEFT JOIN RATING r ON f.RATING_ID = r.RATING_ID WHERE FILM_ID IN " +
            "(SELECT fd.FILM_ID FROM FILM_DIRECTORS fd " +
            "LEFT JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID " +
            "WHERE LOWER(d.DIRECTOR_NAME) like LOWER(?))";
    private static final String FIND_BY_DIRECTOR_NAME_AND_FILM_NAME_QUERY = "SELECT f.*, r.RATING_NAME mpa_name FROM FILMS f " +
            "LEFT JOIN RATING r ON f.RATING_ID = r.RATING_ID WHERE FILM_ID IN " +
            "(SELECT fd.FILM_ID FROM FILM_DIRECTORS fd " +
            "LEFT JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID " +
            "WHERE LOWER(d.DIRECTOR_NAME) like LOWER(?)) and LOWER(f.FILM_NAME) like LOWER(?)";

    @Autowired
    public FilmRepository(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
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
        return count > 0;
    }

    public boolean genreTry(Integer genreId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM GENRE WHERE GENRE_ID = ?",
                Integer.class,
                genreId
        );
        return count > 0;
    }

    public Collection<Film> getCommonFilms(int userId, int friendId) {
        return findMany(GET_COMMON_FILMS_QUERY, userId, friendId);
    }

    @Override
    public Collection<Film> getSearchFilms(String query, String by) {
        Collection<Film> films = new ArrayList<>();
        String[] queryParts = query.split(",");
        String[] byParts = by.split(",");
        if (byParts.length == 1) {
            if (byParts[0].equals("title")) {
                films = findMany(FIND_BY_NAME_QUERY, "%" + query + "%");
            } else if (byParts[0].equals("director")) {
                films = findMany(FIND_BY_DIRECTOR_NAME_QUERY, "%" + query + "%");
            } else {
                throw new ValidationException("Указано неверное значение критерия поиска (by) для поиска фильма." +
                        " Значение переданного критерия поиска (by) = " + by);
            }
        } else if (byParts.length == 2) {
            String directorQuery;
            String titleQuery;
            if (byParts[0].equals("title") && byParts[1].equals("director")) {
                directorQuery = queryParts.length == 2 ? queryParts[1] : null;
                titleQuery = queryParts[0];
            } else if (byParts[0].equals("director") && byParts[1].equals("title")) {
                directorQuery = queryParts[0];
                titleQuery = queryParts.length == 2 ? queryParts[1] : null;
            } else {
                throw new ValidationException("Указано неверное значение критерия поиска (by) для поиска фильма." +
                        " Значение переданного критерия поиска (by) = " + by);
            }

            if (directorQuery == null && titleQuery != null) {
                films = findMany(FIND_BY_NAME_QUERY, "%" + titleQuery + "%");
                films.addAll(findMany(FIND_BY_DIRECTOR_NAME_QUERY, "%" + queryParts[0] + "%"));
            } else if (titleQuery == null && directorQuery != null) {
                films = findMany(FIND_BY_DIRECTOR_NAME_QUERY, "%" + directorQuery + "%");
                films.addAll(films = findMany(FIND_BY_NAME_QUERY, "%" + queryParts[0] + "%"));
            } else if (directorQuery != null) {
                films = findMany(FIND_BY_DIRECTOR_NAME_AND_FILM_NAME_QUERY, "%" + directorQuery + "%", "%" + titleQuery + "%");
            }
        } else {
            throw new ValidationException("Указано неверное значение критерия поиска (by) для поиска фильма." +
                    " Значение переданного критерия поиска (by) = " + by);
        }
        return films.stream().sorted(Comparator.comparing(Film::getCountLikes, Comparator.reverseOrder())).toList();
    }

}
