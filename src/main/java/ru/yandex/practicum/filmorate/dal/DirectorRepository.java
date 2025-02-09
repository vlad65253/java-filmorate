package ru.yandex.practicum.filmorate.dal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class DirectorRepository extends BaseRepository<Director> implements DirectorStorage {

    private final JdbcTemplate jdbc;

    @Autowired
    public DirectorRepository(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
        this.jdbc = jdbc;
    }

    @Override
    public Set<Director> getDirectors() {
        return streamQuery("""
                SELECT DIRECTOR_ID, DIRECTOR_NAME FROM DIRECTORS
                """);
    }

    @Override
    public Optional<Director> getDirectorById(int id) {
        return findOne("""
                SELECT
                DIRECTOR_ID,
                DIRECTOR_NAME
                FROM DIRECTORS WHERE DIRECTOR_ID = ?
                """, id);
    }

    @Override
    public Director createDirector(Director director) {
        int id = insert("""
                INSERT INTO DIRECTORS(DIRECTOR_NAME) VALUES (?)
                """, director.getName());
        director.setId(id);
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        update("""
                    UPDATE DIRECTORS SET DIRECTOR_NAME = ? WHERE DIRECTOR_ID = ?
                """, director.getName(), director.getId());
        return director;
    }

    @Override
    public void deleteDirectorById(int id) {
        jdbc.update("""
                DELETE FROM DIRECTORS WHERE DIRECTOR_ID = ?
                """, id);
    }

    @Override
    public void createDirectorsForFilmById(int filmId, List<Director> directorsId) {
        batchUpdateBase("""
                        INSERT INTO DIRECTORS_SAVE(FILM_ID, DIRECTOR_ID)
                        VALUES (?, ?)
                        """,
                new BatchPreparedStatementSetter() {
                    @SuppressWarnings("NullableProblems")
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, filmId);
                        ps.setLong(2, directorsId.get(i).getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return directorsId.size();
                    }
                }
        );
    }

    @Override
    public Set<Director> getDirectorsFilmById(int filmId) {
        return findMany("""
                SELECT DIRECTOR_ID, DIRECTOR_NAME FROM DIRECTORS WHERE DIRECTOR_ID IN(SELECT DIRECTOR_ID FROM DIRECTORS_SAVE WHERE FILM_ID = ?)
                """, filmId)
                .stream()
                .sorted(Comparator.comparing(Director::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public void deleteDirectorsFilmById(int filmId) {
        delete("""
                DELETE FROM DIRECTORS_SAVE WHERE FILM_ID = ?
                """, filmId);
    }
}
