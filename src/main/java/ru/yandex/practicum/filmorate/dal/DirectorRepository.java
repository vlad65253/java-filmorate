package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class DirectorRepository extends BaseRepository<Director> implements DirectorStorage {

    public DirectorRepository(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Set<Director> getDirectors() {
        String sql = "SELECT DIRECTOR_ID, DIRECTOR_NAME FROM DIRECTORS";
        return streamQuery(sql);
    }

    @Override
    public Director getDirectorById(int id) {
        String sql = "SELECT DIRECTOR_ID, DIRECTOR_NAME FROM DIRECTORS WHERE DIRECTOR_ID = ?";
        return findOne(sql, id)
                .orElseThrow(() -> new NotFoundException("Режиссер с id " + id + " не найден"));
    }

    @Override
    public Director createDirector(Director director) {
        int id = insert("INSERT INTO DIRECTORS (DIRECTOR_NAME) VALUES (?)", director.getName());
        director.setId(id);
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        update("UPDATE DIRECTORS SET DIRECTOR_NAME = ? WHERE DIRECTOR_ID = ?",
                director.getName(), director.getId());
        return director;
    }

    @Override
    public void deleteDirectorById(int id) {
        if (!update("DELETE FROM DIRECTORS WHERE DIRECTOR_ID = ?", id)) {
            throw new NotFoundException("Режиссер с id " + id + " не найден");
        }
    }

    @Override
    public void createDirectorsForFilmById(int filmId, List<Director> directors) {
        batchUpdateBase("INSERT INTO DIRECTORS_SAVE (FILM_ID, DIRECTOR_ID) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, filmId);
                        ps.setInt(2, directors.get(i).getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return directors.size();
                    }
                });
    }

    @Override
    public Set<Director> getDirectorsFilmById(int filmId) {
        String sql = """
                SELECT d.DIRECTOR_ID, d.DIRECTOR_NAME
                FROM DIRECTORS d
                WHERE d.DIRECTOR_ID IN (SELECT DIRECTOR_ID FROM DIRECTORS_SAVE WHERE FILM_ID = ?)
                """;
        return findMany(sql, filmId)
                .stream()
                .sorted(Comparator.comparing(Director::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public void deleteDirectorsFilmById(int filmId) {
        delete("DELETE FROM DIRECTORS_SAVE WHERE FILM_ID = ?", filmId);
    }


    @Override
    public Map<Integer, Set<Director>> getDirectorsForFilmIds(Set<Integer> filmIds) {
        if (filmIds.isEmpty()) {
            return Collections.emptyMap();
        }
        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = String.format(
                "SELECT ds.FILM_ID, d.DIRECTOR_ID, d.DIRECTOR_NAME " +
                "FROM DIRECTORS_SAVE ds " +
                "JOIN DIRECTORS d ON ds.DIRECTOR_ID = d.DIRECTOR_ID " +
                "WHERE ds.FILM_ID IN (%s)",
                placeholders);
        Object[] params = filmIds.toArray();
        Map<Integer, Set<Director>> result = new HashMap<>();
        jdbc.query(sql, params, (ResultSet rs) -> {
            while (rs.next()) {
                int filmId = rs.getInt("FILM_ID");
                int directorId = rs.getInt("DIRECTOR_ID");
                String directorName = rs.getString("DIRECTOR_NAME");
                Director director = new Director(directorId, directorName);
                result.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(director);
            }
            return result;
        });
        return result;
    }
}