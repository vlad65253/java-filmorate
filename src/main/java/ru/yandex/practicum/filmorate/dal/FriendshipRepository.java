package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;

import java.util.List;

@Repository
public class FriendshipRepository extends BaseRepository<User> implements FriendshipStorage {

    public FriendshipRepository(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        update("INSERT INTO FRIENDS_LIST (USER_ID, FRIEND_ID) VALUES (?, ?)", userId, friendId);
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        delete("DELETE FROM FRIENDS_LIST WHERE USER_ID = ? AND FRIEND_ID = ?", userId, friendId);
    }

    @Override
    public List<User> getCommonFriends(int firstUser, int secondUser) {
        return findMany("SELECT * FROM USERS WHERE USER_ID IN " +
                "(SELECT FRIEND_ID FROM FRIENDS_LIST WHERE USER_ID = ?) AND USER_ID IN " +
                "(SELECT FRIEND_ID FROM FRIENDS_LIST WHERE USER_ID = ?)", firstUser, secondUser);
    }

    @Override
    public List<User> getAllUserFriends(int userId) {
        return findMany("SELECT * FROM USERS WHERE USER_ID IN " +
                "(SELECT FRIEND_ID FROM FRIENDS_LIST WHERE USER_ID = ?)", userId);
    }
}