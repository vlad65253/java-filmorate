package ru.yandex.practicum.filmorate.dal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
@Repository
@Qualifier("userRepository")
public class UserRepository extends BaseRepository<User> implements UserStorage {

    private static final String CREATE_USER_QUERY = "INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_USER_QUERY = "UPDATE USERS SET EMAIL = ?, LOGIN = ?, NAME = ?, BIRTHDAY = ? WHERE USER_ID = ?";
    private static final String GET_ALL_USERS_QUERY = "SELECT * FROM USERS";
    private static final String GET_USER_QUERY = "SELECT * FROM USERS WHERE USER_ID = ?";
    private static final String DELETE_USER_QUERY = "DELETE FROM USERS WHERE USER_ID = ?";
    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
        this.jdbc = jdbc;
    }

    @Override
    public User createUser(User user) {
        Integer id = insert(CREATE_USER_QUERY,
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
        boolean updated = update(UPDATE_USER_QUERY,
                updateUser.getEmail(),
                updateUser.getLogin(),
                updateUser.getName(),
                updateUser.getBirthday(),
                updateUser.getId());
        if (!updated) {
            throw new NotFoundException("Пользователь с id " + updateUser.getId() + " не найден");
        }
        return updateUser;
    }

    @Override
    public Collection<User> getUsers() {
        return findMany(GET_ALL_USERS_QUERY);
    }

    @Override
    public User getUserById(Integer id) {
        return findOne(GET_USER_QUERY, id).get();
    }

    @Override
    public void deleteUser(Integer id) {
        boolean deleted = delete(DELETE_USER_QUERY, id);
        if (!deleted) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }

    public Map<Integer, Long> findUsersWithCommonLikes(Set<Integer> filmIds, Integer userId) {
        if (filmIds.isEmpty()) {
            return Collections.emptyMap();
        }
        String placeholders = filmIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = String.format("""
                SELECT USER_ID, COUNT(FILM_ID) AS COMMON_LIKES
                FROM LIKE_LIST
                WHERE FILM_ID IN (%s) AND USER_ID != ?
                GROUP BY USER_ID
                """, placeholders);
        List<Object> params = new ArrayList<>(filmIds);
        params.add(userId);

        List<Map<String, Object>> rows = jdbc.queryForList(sql, params.toArray());
        Map<Integer, Long> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            result.put((Integer) row.get("USER_ID"), (Long) row.get("COMMON_LIKES"));
        }
        return result;
    }
}