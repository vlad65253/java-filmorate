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
    public DirectorRepository(JdbcTemplate jdbc, RowMapper<Director> mapper, DirectorRepository directorRepository) {
        super(jdbc, mapper);
        this.jdbc = jdbc;
    }

    @Override
    public List<Director> getDirectors() {
        return findMany("""
                SELECT id, name FROM Directors
                """, mapper);
    }

    @Override
    public Optional<Director> getDirectorById(long id) {
        return findOne("""
                SELECT
                id,
                name
                FROM Directors WHERE id = ?
                """, id);
    }

    @Override
    public Director createDirector(Director director) {
        int id = insert("""
                INSERT INTO Directors(name)
                VALUES ?
                """, director.getName());
        director.setId(id);
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        update("""
                    UPDATE Directors SET
                    name = :name
                    WHERE id = ?
                """, director.getName());
        return director;
    }

    @Override
    public void deleteDirectorById(long id) {
        jdbc.update("""
                DELETE FROM Directors WHERE id = ?
                """, id);
    }

    @Override
    public void createDirectorsForFilmById(long filmId, List<Director> directorsId) {
        batchUpdateBase("""
                        INSERT INTO Directors_save(film_id, director_id)
                        VALUES (:film_id, :director_id)
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
    public LinkedHashSet<Director> getDirectorsFilmById(long filmId) {
        return findMany("""
                SELECT id, name FROM Directors WHERE id IN(
                SELECT director_id FROM Directors_save WHERE film_id = ?)
                """, filmId, mapper).stream()
                .sorted(Comparator.comparing(Director::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public void deleteDirectorsFilmById(long filmId) {
        update("""
                DELETE FROM Directors_save WHERE film_id = :film_id
                """, filmId);
    }
}
