package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.status.EventOperation;
import ru.yandex.practicum.filmorate.dal.status.EventType;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.LikesStorage;

@Repository
public class LikesRepository extends BaseRepository<Film> implements LikesStorage {
    private static final String ADD_LIKE_FOR_FILM = "INSERT INTO LIKE_LIST (FILM_ID, USER_ID) VALUES (?, ?)";
    private static final String DEL_LIKE_FOR_FILM = "DELETE FROM LIKE_LIST WHERE FILM_ID = ? AND USER_ID = ?";
    private static final String SQL_EVENT = "INSERT INTO EVENTS(USER_ID, EVENT_TYPE, OPERATION, ENTITY_ID) VALUES (?, ?, ?, ?)";

    public LikesRepository(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    public void addLike(Integer filmId, Integer userId) {
        boolean updated = update(ADD_LIKE_FOR_FILM, filmId, userId);
        if (!updated) {
            throw new NotFoundException("Не удалось добавить лайк: фильм с id " + filmId +
                    " или пользователь с id " + userId + " не найден");
        }
        update(SQL_EVENT, userId, EventType.LIKE.toString(), EventOperation.ADD.toString(), filmId);
    }

    public void deleteLike(Integer filmId, Integer userId) {
        boolean deleted = delete(DEL_LIKE_FOR_FILM, filmId, userId);
        if (!deleted) {
            throw new NotFoundException("Лайк для фильма с id " + filmId +
                    " от пользователя " + userId + " не найден");
        }
        update(SQL_EVENT, userId, EventType.LIKE.toString(), EventOperation.REMOVE.toString(), filmId);
    }
}