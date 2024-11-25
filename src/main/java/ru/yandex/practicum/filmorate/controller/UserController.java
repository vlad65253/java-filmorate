package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @PostMapping
    public User createUser(@RequestBody User user) {
        long userId = nextId();
        user.setId(userId);

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("Ошибка валидации логина");
            throw new ValidationException("Ошибка валидации логина.");
        }
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error("Ошибка валидации Емаила");
            throw new ValidationException("Ошибка валидации Емаила.");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("поменял имя на логин");
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Ошибка валидации Дня Рождения");
            throw new ValidationException("Ошибка валидации Дня Рождения.");
        }
        users.put(userId, user);
        log.info("Добавился новый пользователь");
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User updateUser) {
        User userTemp = users.get(updateUser.getId());
        if (updateUser.getEmail() == null) {
            updateUser.setEmail(userTemp.getEmail());
        }
        if (updateUser.getName() == null) {
            updateUser.setName(userTemp.getName());
        }
        if (updateUser.getBirthday() == null) {
            updateUser.setBirthday(userTemp.getBirthday());
        }
        if (updateUser.getLogin() == null) {
            updateUser.setLogin(userTemp.getLogin());
        }
        if (updateUser.getLogin().contains(" ") || !updateUser.getEmail().contains("@")) {
            log.error("Ошибка валидации логина");
            throw new ValidationException("Ошибка валидации логина.");
        }
        if (updateUser.getBirthday().isAfter(LocalDate.now())) {
            log.error("Ошибка валидации Дня Рождения");
            throw new ValidationException("Ошибка валидации Дня Рождения.");
        }
        if (!users.containsKey(updateUser.getId())) {
            throw new ValidationException("Пользователя с таким айди нет.");
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
