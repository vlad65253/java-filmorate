package ru.yandex.practicum.filmorate.dal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.ResultSet;
import java.util.*;

@Repository
@Qualifier("filmRepository")
public class FilmRepository extends BaseRepository<Film> implements FilmStorage {
    private static final String CREATE_FILM_QUERY = "INSERT INTO FILMS(FILM_NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATING_ID) " +
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
    private static final String GET_TOP_FILMS_QUERY = "SELECT * FROM FILMS f LEFT JOIN RATING r " +
            "ON f.RATING_ID = r.RATING_ID LEFT JOIN (SELECT FILM_ID, COUNT(FILM_ID) AS LIKES FROM LIKE_LIST " +
            "GROUP BY FILM_ID) fl ON f.FILM_ID = fl.FILM_ID ORDER BY LIKES DESC LIMIT ?";
    private static final String DELETE_FILM_QUERY = "DELETE FROM FILMS WHERE FILM_ID = ?";

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
        if(!update(UPDATE_FILM_QUERY,
                filmUpdated.getName(),
                filmUpdated.getDescription(),
                filmUpdated.getReleaseDate(),
                filmUpdated.getDuration(),
                filmUpdated.getMpa().getId(),
                filmUpdated.getId())){
            return filmUpdated;
        } else{
            return null;
        }
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
    public Collection<Film> getTopFilms(Integer count){
        Collection<Film> films = findMany(GET_TOP_FILMS_QUERY, count);
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


}
