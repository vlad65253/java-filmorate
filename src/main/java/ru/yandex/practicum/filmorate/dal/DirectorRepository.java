package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;

@Repository
public class DirectorRepository extends BaseRepository<Director> implements DirectorStorage {
    private static final String GET_ALL_DIRECTORS = "SELECT * FROM DIRECTORS";
    private static final String GET_DIRECTOR_BY_ID = "SELECT * FROM DIRECTORS WHERE DIRECTOR_ID = ?";
    private static final String ADD_DIRECTOR_FOR_DIRECTORS = "INSERT INTO DIRECTORS (DIRECTOR_NAME) VALUES (?)";
    private static final String UPDATE_DIRECTOR_FOR_DIRECTORS = "UPDATE DIRECTORS SET DIRECTOR_NAME = ? WHERE DIRECTOR_ID = ?";
    private static final String ADD_DIRECTOR_QUERY = "INSERT INTO FILM_DIRECTORS (FILM_ID, DIRECTOR_ID) VALUES (?, ?)";
    private static final String DEL_DIRECTOR_QUERY = "DELETE FROM FILM_DIRECTORS WHERE FILM_ID = ?";
    private static final String DEL_DIRECTOR_ID_QUERY = "DELETE FROM DIRECTORS WHERE DIRECTOR_ID = ?";
    private static final String FIND_ALL_BY_FILM_ID_QUERY = "SELECT d.DIRECTOR_ID, d.DIRECTOR_NAME FROM FILM_DIRECTORS fd JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID WHERE fd.FILM_ID = ?";
    private static final String FIND_ALL_BY_FILMS = "SELECT fd.FILM_ID, d.DIRECTOR_ID, d.DIRECTOR_NAME FROM FILM_DIRECTORS fd JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID";

    public DirectorRepository(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
    }

    public Collection<Director> getAllDirectors() {
        return findMany(GET_ALL_DIRECTORS);
    }

    public Director getDirectorById(Integer id) {
        Director director = findOne(GET_DIRECTOR_BY_ID, id);
        if (director == null) {
            throw new NotFoundException("Режиссёр с id " + id + " не найден");
        }
        return director;
    }

    public void addDirector(Integer filmId, List<Integer> directorIds) {
        batchUpdateBase(ADD_DIRECTOR_QUERY, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, filmId);
                ps.setInt(2, directorIds.get(i));
            }
            @Override
            public int getBatchSize() {
                return directorIds.size();
            }
        });
    }

    @Override
    public void delDirector(Integer filmId) {
        // Здесь удаляем связи для фильма – если ничего не удалено, можно не выбрасывать исключение,
        // так как отсутствие связей может быть нормальной ситуацией.
        delete(DEL_DIRECTOR_QUERY, filmId);
    }

    public void delDirectorTable(Integer directorId) {
        boolean delete = delete(DEL_DIRECTOR_ID_QUERY, directorId);
        if (!delete) {
            throw new NotFoundException("Режиссёр с id " + directorId + " не найден");
        }
    }

    public Director createDirector(Director director) {
        Integer id = insert(ADD_DIRECTOR_FOR_DIRECTORS, director.getName());
        if (id == null) {
            throw new ValidationException("Ошибка создания режиссёра");
        }
        director.setId(id);
        return director;
    }

    public Director updateDirector(Director director) {
        boolean updated = update(UPDATE_DIRECTOR_FOR_DIRECTORS, director.getName(), director.getId());
        if (!updated) {
            throw new NotFoundException("Режиссёр с id " + director.getId() + " не найден");
        }
        return director;
    }

    @Override
    public Collection<Director> getAllDirectorsByFilmId(Integer filmId) {
        return findMany(FIND_ALL_BY_FILM_ID_QUERY, filmId);
    }

    @Override
    public Map<Integer, List<Director>> findAllByFilms() {
        List<Map<String, Object>> rows = jdbc.queryForList(FIND_ALL_BY_FILMS);
        return rows.stream().collect(groupingBy(
                row -> ((Number) row.get("FILM_ID")).intValue(),
                mapping(row -> Director.builder()
                        .id((Integer) row.get("DIRECTOR_ID"))
                        .name((String) row.get("DIRECTOR_NAME"))
                        .build(), Collectors.toList())
        ));
    }

    // Новый метод для получения режиссёров для конкретного фильма
    public Collection<Director> getDirectorsByFilm(int filmId) {
        return findMany(FIND_ALL_BY_FILM_ID_QUERY, filmId);
    }
}