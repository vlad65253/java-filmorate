package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.FriendshipRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;


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

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(Integer id) {
        return userStorage.getUserById(id);
    }

    public void deleteUser(Integer id) {
        userStorage.deleteUser(id);
    }

    public void addFriend(Integer userId, Integer friendId) {
        getUserById(userId);
        getUserById(friendId);
        friendshipRepository.addFriend(userId, friendId);
        log.info("Пользователь {} добавил в друзья {}", userId, friendId);
    }

    public void deleteFriend(Integer userId, Integer friendId) {
        getUserById(userId);
        getUserById(friendId);
        friendshipRepository.deleteFriend(userId, friendId);
        log.info("Пользователь {} удалил из друзей {}", userId, friendId);
    }

    public Collection<User> getFriends(Integer userId) {
        getUserById(userId);
        return friendshipRepository.getAllUserFriends(userId);
    }

    public Collection<User> getCommonFriend(Integer firstId, Integer secondId) {
        getUserById(firstId);
        getUserById(secondId);
        return friendshipRepository.getCommonFriends(firstId, secondId);
    }

    public Collection<Film> getRecommendations(Integer userId) {
        Set<Integer> userLikedFilms = filmRepository.getLikedFilmsByUser(userId);

        Map<Integer, Long> commonLikesCount = userRepository.findUsersWithCommonLikes(userLikedFilms, userId);

        Integer similarUserId = commonLikesCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (similarUserId == null) {
            return Collections.emptyList();
        }

        Set<Integer> similarUserLikedFilms = filmRepository.getLikedFilmsByUser(similarUserId);
        similarUserLikedFilms.removeAll(userLikedFilms);

        return filmRepository.findFilmsByIds(similarUserLikedFilms);
    }
}

