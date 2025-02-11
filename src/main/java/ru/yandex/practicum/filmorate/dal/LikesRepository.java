package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.LikesStorage;

import java.util.*;

@Repository
public class LikesRepository extends BaseRepository<Film> implements LikesStorage {

    public LikesRepository(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public void addLike(int filmId, int userId) {
        update("INSERT INTO LIKE_LIST (FILM_ID, USER_ID) VALUES (?, ?)", filmId, userId);
    }

    @Override
    public void deleteLike(int filmId, int userId) {
        delete("DELETE FROM LIKE_LIST WHERE FILM_ID = ? AND USER_ID = ?", filmId, userId);
    }

    @Override
    public int getLikeCountForFilm(int filmId) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM LIKE_LIST WHERE FILM_ID = ?", Integer.class, filmId);
    }

    @Override
    public Set<Integer> getLikedFilmsByUser(int userId) {
        return new HashSet<>(jdbc.queryForList("SELECT FILM_ID FROM LIKE_LIST WHERE USER_ID = ?", Integer.class, userId));
    }

    public Map<Integer, Long> getCommonLikes(String sql, Object[] params) {
        return jdbc.query(sql, params, rs -> {
            Map<Integer, Long> result = new HashMap<>();
            while (rs.next()) {
                result.put(rs.getInt("USER_ID"), rs.getLong("COMMON_LIKES"));
            }
            return result;
        });
    }
}