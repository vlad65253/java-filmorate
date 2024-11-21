package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private final static Logger log = LoggerFactory.getLogger(UserController.class);

    @PostMapping
    public User createUser(@RequestBody User user) {
        long userId = nextId();
        user.setId(userId);

        if (user.getLogin().contains(" ")) {
            log.error("логин с пробелами");
            throw new ValidationException("Логин не должен содержать пробелов.");
        }
        if (user.getLogin().isEmpty()) {
            log.error("пустой логин");
            throw new ValidationException("Логин не может быть пустым.");
        }
        if (user.getEmail().isEmpty()) {
            log.error("емаил пустой");
            throw new ValidationException("Email не может быть пустым.");
        }
        if (!user.getEmail().contains("@")) {
            log.error("емаил без @");
            throw new ValidationException("В Email-e должен быть символ @.");
        }
        if (user.getName().isEmpty()) {
            user.setName(user.getLogin());
            log.info("поменял имя на логин");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("др в будующем");
            throw new ValidationException("Пользователь не может родиться в будующем).");
        }
        users.put(userId, user);
        log.info("Добавился новый пользователь");
        return user;
    }

    @PutMapping
    public User updateFilm(@RequestBody User updateUser) {
        if (updateUser.getLogin().contains(" ")) {
            log.error("логин с пробелами");
            throw new ValidationException("Логин не должен содержать пробелов.");
        }
        if (updateUser.getLogin().isEmpty()) {
            log.error("пустой логин");
            throw new ValidationException("Логин не может быть пустым.");
        }
        if (updateUser.getEmail().isEmpty()) {
            log.error("емаил пустой");
            throw new ValidationException("Email не может быть пустым.");
        }
        if (!updateUser.getEmail().contains("@")) {
            log.error("емаил без @");
            throw new ValidationException("В Email-e должен быть символ @.");
        }
        if (updateUser.getName().isEmpty()) {
            updateUser.setName(updateUser.getLogin());
        }
        if (!users.containsKey(updateUser.getId())) {
            log.error("пользователя с " + updateUser.getId() + " айди нет");
            throw new ValidationException("Ползователя с таким айди нет.");
        }
        users.put(updateUser.getId(), updateUser);
        log.info("Обновился пользователь с айди = " + updateUser.getId());
        return updateUser;
    }

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    private long nextId() {
        long nowMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++nowMaxId;
    }
}
