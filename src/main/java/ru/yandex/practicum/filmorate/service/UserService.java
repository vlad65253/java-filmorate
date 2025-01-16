package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FriendshipRepository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipRepository friendshipRepository;

    public UserService(@Autowired @Qualifier("userRepository") UserStorage userStorage, @Autowired FriendshipRepository friendshipRepository) {
        this.userStorage = userStorage;
        this.friendshipRepository = friendshipRepository;
    }

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
        log.info("User {} added friend {}", userId, friendId);
    }

    public void deleteFriend(Integer userId, Integer friendId) {
        getUserById(userId);
        getUserById(friendId);
        friendshipRepository.deleteFriend(userId, friendId);
        log.info("User {} deleted friend {}", userId, friendId);
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


}
