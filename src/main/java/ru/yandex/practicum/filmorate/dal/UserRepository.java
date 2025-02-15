package ru.yandex.practicum.filmorate.dal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Repository
@Qualifier("userRepository")
public class UserRepository extends BaseRepository<User> implements UserStorage {

    public UserRepository(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public User createUser(User user) {
        Integer id = insert("INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY) VALUES (?, ?, ?, ?)",
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday());
        if (id == null || id == 0) {
            throw new ValidationException("Ошибка создания пользователя");
        }
        user.setId(id);
        return user;
    }

    @Override
    public User updateUser(User updateUser) {
        update("UPDATE USERS SET EMAIL = ?, LOGIN = ?, NAME = ?, BIRTHDAY = ? WHERE USER_ID = ?",
                updateUser.getEmail(),
                updateUser.getLogin(),
                updateUser.getName(),
                updateUser.getBirthday(),
                updateUser.getId());
        return updateUser;
    }

    @Override
    public List<User> getUsers() {
        return findMany("SELECT * FROM USERS");
    }

    @Override
    public User getUserById(int id) {
        return findOne("SELECT * FROM USERS WHERE USER_ID = ?", id).get();
    }

    @Override
    public void deleteUser(int id) {
        delete("DELETE FROM USERS WHERE USER_ID = ?", id);
    }
}