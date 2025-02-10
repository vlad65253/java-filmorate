package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.status.EventOperation;
import ru.yandex.practicum.filmorate.dal.status.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.LikesStorage;

import java.util.*;

@Repository
public class LikesRepository extends BaseRepository<Film> implements LikesStorage {

    public LikesRepository(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        update("INSERT INTO LIKE_LIST (FILM_ID, USER_ID) VALUES (?, ?)", filmId, userId);
        update("INSERT INTO EVENTS(USER_ID, EVENT_TYPE, OPERATION, ENTITY_ID) VALUES (?, ?, ?, ?)", userId, EventType.LIKE.toString(), EventOperation.ADD.toString(), filmId);
    }

    @Override
    public void deleteLike(Integer filmId, Integer userId) {
        delete("DELETE FROM LIKE_LIST WHERE FILM_ID = ? AND USER_ID = ?", filmId, userId);
        update("INSERT INTO EVENTS(USER_ID, EVENT_TYPE, OPERATION, ENTITY_ID) VALUES (?, ?, ?, ?)", userId, EventType.LIKE.toString(), EventOperation.REMOVE.toString(), filmId);
    }

    @Override
    public int getLikeCountForFilm(Integer filmId) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM LIKE_LIST WHERE FILM_ID = ?", Integer.class, filmId);
    }

    @Override
    public Set<Integer> getLikedFilmsByUser(Integer userId) {
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