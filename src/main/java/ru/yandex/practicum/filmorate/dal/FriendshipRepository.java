package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;

import java.util.List;

@Repository
public class FriendshipRepository extends BaseRepository<User> implements FriendshipStorage {

    public FriendshipRepository(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        boolean updated = update("INSERT INTO FRIENDS_LIST (USER_ID, FRIEND_ID) VALUES (?, ?)", userId, friendId);
        if (!updated) {
            throw new NotFoundException("Не удалось добавить друга: пользователь с id " + userId +
                    " или друг с id " + friendId + " не найден");
        }
        update("INSERT INTO EVENTS(USER_ID, EVENT_TYPE, OPERATION, ENTITY_ID) VALUES (?, ?, ?, ?)", userId, "FRIEND", "ADD", friendId);
    }

    @Override
    public void deleteFriend(Integer userId, Integer friendId) {
        boolean deleted = delete("DELETE FROM FRIENDS_LIST WHERE USER_ID = ? AND FRIEND_ID = ?", userId, friendId);
        if (!deleted) {
            throw new NotFoundException("Не удалось удалить друга: дружба между пользователями с id " +
                    userId + " и " + friendId + " не найдена");
        }
        update("INSERT INTO EVENTS(USER_ID, EVENT_TYPE, OPERATION, ENTITY_ID) VALUES (?, ?, ?, ?)", userId, "FRIEND", "REMOVE", friendId);
    }

    @Override
    public List<User> getCommonFriends(Integer firstUser, Integer secondUser) {
        return findMany("SELECT * FROM USERS WHERE USER_ID IN " +
                "(SELECT FRIEND_ID FROM FRIENDS_LIST WHERE USER_ID = ?) AND USER_ID IN " +
                "(SELECT FRIEND_ID FROM FRIENDS_LIST WHERE USER_ID = ?)", firstUser, secondUser);
    }

    @Override
    public List<User> getAllUserFriends(Integer userId) {
        return findMany("SELECT * FROM USERS WHERE USER_ID IN " +
                "(SELECT FRIEND_ID FROM FRIENDS_LIST WHERE USER_ID = ?)", userId);
    }
}