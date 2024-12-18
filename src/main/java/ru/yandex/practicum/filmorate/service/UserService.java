package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User createUser(User user) {
        return userStorage.createUser(user);
    }

    public User updateUser(User updateUser) {
        return userStorage.updateUser(updateUser);
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(long id) {
        return userStorage.getUserById(id);
    }

    public void deleteUser(long id) {
        userStorage.deleteUser(id);
    }

    public void addFriend(long userId, long friendId) {
        User tempUserOne = userStorage.getUserById(userId);
        User tempUserTwo = userStorage.getUserById(friendId);

        tempUserOne.getFriends().add(friendId);
        log.info("Пользователь {} подружился с {}", userId, friendId);
        tempUserTwo.getFriends().add(userId);
        log.info("Пользователь {} подружился с {}", friendId, userId);
    }

    public void deleteFriend(long userId, long friendId) {
        User tempUserOne = userStorage.getUserById(userId);
        User tempUserTwo = userStorage.getUserById(friendId);

        tempUserOne.getFriends().remove(friendId);
        log.info("Пользователь {} убрал из друзей {}", friendId, userId);
        tempUserTwo.getFriends().remove(userId);
        log.info("Пользователь {} убрал из друзей {}", userId, friendId);
    }

    public Collection<User> getFriends(long userId) {
        return userStorage.getUserById(userId).getFriends().stream()
                .map(userStorage::getUserById)
                .toList();
    }

    public Collection<User> getGeneralFriend(long firstId, long secondId) {
        User user = userStorage.getUserById(firstId);
        User friend = userStorage.getUserById(secondId);

        //Запомнить реализацию нахождения общих друзей
        return user.getFriends().stream()
                .filter(userId -> friend.getFriends().contains(userId))
                .map(userStorage::getUserById)
                .toList();
    }


}
