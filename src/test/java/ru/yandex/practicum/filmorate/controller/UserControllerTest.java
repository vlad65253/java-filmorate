package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @Test
    void createUserValidUserShouldReturnUser() {
        User user = new User();
        user.setLogin("validLogin");
        user.setEmail("email@example.com");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userController.createUser(user);

        assertNotNull(createdUser.getId());
        assertEquals(user.getLogin(), createdUser.getLogin());
        assertEquals(user.getEmail(), createdUser.getEmail());
        assertEquals(user.getName(), createdUser.getName());
        assertEquals(user.getBirthday(), createdUser.getBirthday());
    }

    @Test
    void createUserLoginWithSpacesShouldThrowValidationException() {
        User user = new User();
        user.setLogin("invalid login");
        user.setEmail("email@example.com");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void createUserEmptyLoginShouldThrowValidationException() {
        User user = new User();
        user.setLogin("");
        user.setEmail("email@example.com");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void createUserEmptyEmailShouldThrowValidationException() {
        User user = new User();
        user.setLogin("validLogin");
        user.setEmail("");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void createUserEmailWithoutAtSymbolShouldThrowValidationException() {
        User user = new User();
        user.setLogin("validLogin");
        user.setEmail("invalidemail.com");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void createUserFutureBirthdayShouldThrowValidationException() {
        User user = new User();
        user.setLogin("validLogin");
        user.setEmail("email@example.com");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void updateUserValidUserShouldReturnUpdatedUser() {
        User user = new User();
        user.setLogin("validLogin");
        user.setEmail("email@example.com");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userController.createUser(user);

        createdUser.setName("Updated Name");
        User updatedUser = userController.updateFilm(createdUser);

        assertEquals("Updated Name", updatedUser.getName());
    }

    @Test
    void updateUserNonExistingIdShouldThrowValidationException() {
        User user = new User();
        user.setId(999L); // Несуществующий ID
        user.setLogin("validLogin");
        user.setEmail("email@example.com");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> userController.updateFilm(user));
    }

    @Test
    void getUsersShouldReturnAllUsers() {
        User user1 = new User();
        user1.setLogin("login1");
        user1.setEmail("email1@example.com");
        user1.setName("Name1");
        user1.setBirthday(LocalDate.of(2000, 1, 1));

        User user2 = new User();
        user2.setLogin("login2");
        user2.setEmail("email2@example.com");
        user2.setName("Name2");
        user2.setBirthday(LocalDate.of(1990, 1, 1));

        userController.createUser(user1);
        userController.createUser(user2);

        Collection<User> users = userController.getUsers();

        assertEquals(2, users.size());
    }
}
