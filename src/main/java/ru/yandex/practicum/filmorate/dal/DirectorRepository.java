package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
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
    private static final String GET_DIRECTOR_BY_ID = "SELECT * FROM DIRECTORS " +
            "WHERE DIRECTOR_ID = ?";
    private static final String ADD_DIRECTOR_FOR_DIRECTORS = "INSERT INTO DIRECTORS (DIRECTOR_NAME) VALUES (?)";

    private static final String UPDATE_DIRECTOR_FOR_DIRECTORS = "UPDATE DIRECTORS SET DIRECTOR_NAME = ? WHERE DIRECTOR_ID = ?";
    private static final String ADD_DIRECTOR_QUERY = "INSERT INTO FILM_DIRECTORS (FILM_ID, DIRECTOR_ID) VALUES (?, ?)";
    private static final String DEL_DIRECTOR_QUERY = "DELETE FROM FILM_DIRECTORS WHERE FILM_ID = ?";
    private static final String FIND_ALL_BY_FILM_ID_QUERY = """
             SELECT fd.DIRECTOR_ID, d.DIRECTOR_NAME FROM FILM_DIRECTORS fd
             JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID
             WHERE FILM_ID = ?
            """;
    private static final String FIND_ALL_BY_FILMS = """
            SELECT fd.FILM_ID, d.*
            FROM FILM_DIRECTORS fd
            JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID
            """;

    public DirectorRepository(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
    }

    public Collection<Director> getAllDirectors() {
        return findMany(GET_ALL_DIRECTORS);
    }

    public Director getDirectorById(Integer id) {
        return findOne(GET_DIRECTOR_BY_ID, id);
    }

    public void addDirector(Integer filmId, List<Integer> directorId) {
        batchUpdateBase(ADD_DIRECTOR_QUERY, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, filmId);
                ps.setLong(2, directorId.get(i));
            }

            @Override
            public int getBatchSize() {
                return directorId.size();
            }
        });
    }

    public void delDirector(long id) {
        delete(DEL_DIRECTOR_QUERY, id);
    }

    public boolean directorExists(Integer directorId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM DIRECTORS WHERE DIRECTOR_ID = ?",
                Integer.class,
                directorId
        );
        return count != null && count > 0;
    }

    public Director createDirector(Director director) {
        Integer id = insert(ADD_DIRECTOR_FOR_DIRECTORS,
                director.getName());
        director.setId(id);
        return director;
    }

    public Director updateDirector(Director director) {
        update(UPDATE_DIRECTOR_FOR_DIRECTORS,
                director.getName(),
                director.getId());
        return director;
    }

    @Override
    public Collection<Director> getAllDirectorsByFilmId(Integer id) {
        return findMany(FIND_ALL_BY_FILM_ID_QUERY, id);
    }

    @Override
    public Map<Integer, List<Director>> findAllByFilms() {
        List<Map<String, Object>> rows = jdbc.queryForList(FIND_ALL_BY_FILMS);

        // 🔹 Выводим в логи, какие данные загружаются
        System.out.println("DEBUG: Загруженные режиссёры: " + rows);

        return rows.stream().collect(groupingBy(
                row -> ((Number) row.get("FILM_ID")).intValue(),
                mapping(row -> Director.builder()
                        .id((Integer) row.get("DIRECTOR_ID"))
                        .name((String) row.get("DIRECTOR_NAME")) // ✅ Проверь, что имя колонки совпадает с таблицей
                        .build(), Collectors.toList())
        ));
    }

}