package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.status.EventOperation;
import ru.yandex.practicum.filmorate.dal.status.EventType;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;
    private final EventStorage eventStorage;

    public User createUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.createUser(user);
    }

    public User updateUser(User updateUser) {
        getUserById(updateUser.getId());
        return userStorage.updateUser(updateUser);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(int id) {
        return userStorage.getUserById(id);
    }

    public void deleteUser(int id) {
        eventStorage.deleteEventsByUserId(id);
        userStorage.deleteUser(id);
    }

    public void addFriend(int userId, int friendId) {
        getUserById(userId);
        getUserById(friendId);
        friendshipStorage.addFriend(userId, friendId);
        log.info("Пользователь {} добавил в друзья {}", userId, friendId);
        eventStorage.addEvent(userId, EventType.FRIEND, EventOperation.ADD, friendId);
    }

    public void deleteFriend(int userId, int friendId) {
        getUserById(userId);
        getUserById(friendId);
        friendshipStorage.deleteFriend(userId, friendId);
        log.info("Пользователь {} удалил из друзей {}", userId, friendId);
        eventStorage.addEvent(userId, EventType.FRIEND, EventOperation.REMOVE, friendId);
    }

    public List<User> getFriends(int userId) {
        getUserById(userId);
        return friendshipStorage.getAllUserFriends(userId);
    }

    public List<User> getCommonFriend(int firstId, int secondId) {
        getUserById(firstId);
        getUserById(secondId);
        return friendshipStorage.getCommonFriends(firstId, secondId);
    }

    public LinkedList<Event> getFeedUserById(int id) {
        LinkedList<Event> events = eventStorage.getFeedUserById(id).stream()
                .sorted(Comparator.comparing(Event::getTimestamp))
                .collect(Collectors.toCollection(LinkedList::new));
        if (events.isEmpty()) {
            throw new NotFoundException("Лента событий для пользователя с id " + id + " пуста.");
        }
        return events;
    }

}