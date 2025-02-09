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
    private static final String CREATE_FILM_QUERY = """
            INSERT INTO FILMS (FILM_NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATING_ID)
            VALUES (?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_FILM_QUERY = """
            UPDATE FILMS
            SET FILM_NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ?, RATING_ID = ?
            WHERE FILM_ID = ?
            """;
    private static final String GET_ALL_FILMS_QUERY = """
            SELECT *
            FROM FILMS
            """;
    private static final String GET_FILM_QUERY = """
            SELECT f.*, r.RATING_NAME
            FROM FILMS f
            JOIN RATING r ON f.RATING_ID = r.RATING_ID
            WHERE f.FILM_ID = ?
            """;

    private static final String DELETE_FILM_QUERY = "DELETE FROM FILMS WHERE FILM_ID = ?";

    // Топ фильмов без фильтров – изменён алиас для лайков на COUNT_LIKES
    private static final String QUERY_TOP_FILMS = """
            SELECT f.*, r.RATING_NAME, COALESCE(fl.LIKES, 0) AS COUNT_LIKES
            FROM FILMS f
            JOIN RATING r ON f.RATING_ID = r.RATING_ID
            LEFT JOIN (
                SELECT FILM_ID, COUNT(*) AS LIKES
                FROM LIKE_LIST
                GROUP BY FILM_ID
            ) fl ON f.FILM_ID = fl.FILM_ID
            ORDER BY COUNT_LIKES DESC
            LIMIT ?
            """;

    // Фильмы режиссёра
    private static final String FIND_FILMS_BY_DIRECTOR_ID_QUERY = """
            SELECT f.*, r.RATING_NAME, COALESCE(COUNT(l.FILM_ID), 0) AS COUNT_LIKES
            FROM FILMS f
            JOIN FILM_DIRECTORS fd ON f.FILM_ID = fd.FILM_ID
            JOIN RATING r ON f.RATING_ID = r.RATING_ID
            LEFT JOIN LIKE_LIST l ON f.FILM_ID = l.FILM_ID
            WHERE fd.DIRECTOR_ID = ?
            GROUP BY f.FILM_ID, r.RATING_NAME, f.FILM_NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, f.RATING_ID
            """;

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

    private static final String FIND_FILMS_BY_IDS_SQL = """
            SELECT f.*, r.RATING_NAME
            FROM FILMS f
            JOIN RATING r ON f.RATING_ID = r.RATING_ID
            WHERE f.FILM_ID IN (%s)
            """;

    // Топ фильмов с фильтрами по жанру и году
    private static final String GET_TOP_FILMS_SQL = """
            SELECT f.*, r.RATING_NAME, COUNT(DISTINCT l.USER_ID) AS COUNT_LIKES
            FROM FILMS f
            JOIN RATING r ON f.RATING_ID = r.RATING_ID
            LEFT JOIN LIKE_LIST l ON f.FILM_ID = l.FILM_ID
            LEFT JOIN FILMS_GENRE fg ON f.FILM_ID = fg.FILM_ID
            WHERE
                (fg.GENRE_ID = ? OR ? IS NULL)
                AND (EXTRACT(YEAR FROM f.RELEASE_DATE) = ? OR ? IS NULL)
            GROUP BY f.FILM_ID, r.RATING_NAME, f.FILM_NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, f.RATING_ID
            ORDER BY COUNT_LIKES DESC
            LIMIT ?
            """;

    // Поисковые запросы
    private static final String FIND_BY_NAME_QUERY = """
            SELECT f.*, r.RATING_NAME
            FROM FILMS f
            LEFT JOIN RATING r ON f.RATING_ID = r.RATING_ID
            WHERE LOWER(f.FILM_NAME) LIKE LOWER(?)
            """;

    private static final String FIND_BY_DIRECTOR_NAME_QUERY = """
            SELECT f.*, r.RATING_NAME
            FROM FILMS f
            LEFT JOIN RATING r ON f.RATING_ID = r.RATING_ID
            WHERE f.FILM_ID IN (
                SELECT fd.FILM_ID
                FROM FILM_DIRECTORS fd
                JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID
                WHERE LOWER(d.DIRECTOR_NAME) LIKE LOWER(?)
            )
            """;

    // Новый поисковый запрос для случая, когда необходимо искать по названию ИЛИ по режиссёру
    private static final String FIND_BY_DIRECTOR_NAME_OR_FILM_NAME_QUERY = """
            SELECT f.*, r.RATING_NAME
            FROM FILMS f
            LEFT JOIN RATING r ON f.RATING_ID = r.RATING_ID
            WHERE LOWER(f.FILM_NAME) LIKE LOWER(?)
               OR f.FILM_ID IN (
                   SELECT fd.FILM_ID
                   FROM FILM_DIRECTORS fd
                   JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID
                   WHERE LOWER(d.DIRECTOR_NAME) LIKE LOWER(?)
               )
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
    public Set<Film> getTopFilms(int count) {
        return streamQuery("""
                SELECT F.*, COUNT(L.FILM_ID) AS count
                FROM FILMS AS F
                JOIN LIKE_LIST AS L ON L.FILM_ID = F.FILM_ID
                GROUP BY F.FILM_ID
                ORDER BY count DESC
                LIMIT ?
                """, count);
    }

    @Override
    public Collection<Film> getCommonFilms(int userId, int friendId) {
        Collection<Film> films = findMany(GET_COMMON_FILMS_QUERY, userId, friendId);
        return films;
    }

//    @Override
//    public Collection<Film> getByDirectorId(int directorId, String sortBy) {
//        Collection<Film> films = findMany(FIND_FILMS_BY_DIRECTOR_ID_QUERY, directorId);
//        List<Film> filmList = new ArrayList<>(films);
//        if ("year".equalsIgnoreCase(sortBy)) {
//            filmList.sort(Comparator.comparing(Film::getReleaseDate));
//        } else if ("likes".equalsIgnoreCase(sortBy)) {
//            filmList.sort(Comparator.comparingInt(Film::getCountLikes).reversed());
//        }
//        return filmList;
//    }

//    @Override
//    public Collection<Film> getSearchFilms(String query, String by) {
//        Collection<Film> films;
//        String[] byParts = by.split(",");
//        if (byParts.length == 1) {
//            if ("title".equalsIgnoreCase(byParts[0])) {
//                films = findMany(FIND_BY_NAME_QUERY, "%" + query + "%");
//            } else if ("director".equalsIgnoreCase(byParts[0])) {
//                films = findMany(FIND_BY_DIRECTOR_NAME_QUERY, "%" + query + "%");
//            } else {
//                throw new ValidationException("Указано неверное значение критерия поиска (by) для поиска фильма: " + by);
//            }
//        } else if (byParts.length >= 2) {
//            // При наличии двух (и более) критериев ищем фильмы, удовлетворяющие хотя бы одному условию (ИЛИ)
//            films = findMany(FIND_BY_DIRECTOR_NAME_OR_FILM_NAME_QUERY, "%" + query + "%", "%" + query + "%");
//        } else {
//            throw new ValidationException("Указано неверное значение критерия поиска (by): " + by);
//        }
//        films.forEach(film -> {
//        });
//        // Сортируем по количеству лайков в порядке убывания
//        return films.stream()
//                .sorted(Comparator.comparing(Film::getCountLikes, Comparator.reverseOrder()))
//                .toList();
//    }

    public boolean ratingExists(Integer ratingId) {
        Integer count = jdbc.queryForObject(QUERY_EXISTS_RATING, Integer.class, ratingId);
        return count > 0;
    }

    public Collection<Film> findFilmsByIds(Set<Integer> filmIds) {
        if (filmIds.isEmpty()) {
            return Collections.emptyList();
        }
        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = String.format(FIND_FILMS_BY_IDS_SQL, placeholders);
        return findMany(sql, filmIds.toArray());
    }

    public Set<Integer> getLikedFilmsByUser(Integer userId) {
        String sql = "SELECT FILM_ID FROM LIKE_LIST WHERE USER_ID = ?";
        return new HashSet<>(jdbc.queryForList(sql, Integer.class, userId));
    }
}