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

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.createUser(user));
        assertEquals("Ошибка валидации логина.", exception.getMessage());
    }

    @Test
    void createUserEmptyLoginShouldThrowValidationException() {
        User user = new User();
        user.setLogin("");
        user.setEmail("email@example.com");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.createUser(user));
        assertEquals("Ошибка валидации логина.", exception.getMessage());
    }

    @Test
    void createUserEmptyEmailShouldThrowValidationException() {
        User user = new User();
        user.setLogin("validLogin");
        user.setEmail("");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.createUser(user));
        assertEquals("Ошибка валидации Емаила.", exception.getMessage());
    }

    @Test
    void createUserEmailWithoutAtSymbolShouldThrowValidationException() {
        User user = new User();
        user.setLogin("validLogin");
        user.setEmail("invalidemail.com");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.createUser(user));
        assertEquals("Ошибка валидации Емаила.", exception.getMessage());
    }

    @Test
    void createUserFutureBirthdayShouldThrowValidationException() {
        User user = new User();
        user.setLogin("validLogin");
        user.setEmail("email@example.com");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().plusDays(1));

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.createUser(user));
        assertEquals("Ошибка валидации Дня Рождения.", exception.getMessage());
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
        User updatedUser = userController.updateUser(createdUser);

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

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.updateUser(user));
        assertEquals("Пользователя с таким айди нет.", exception.getMessage());
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

    @Test
    void createUserNullBirthdayShouldThrowValidationException() {
        User user = new User();
        user.setLogin("validLogin");
        user.setEmail("email@example.com");
        user.setName("Valid Name");
        user.setBirthday(null); // Null birthday

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.createUser(user));
        assertEquals("Ошибка валидации Дня Рождения.", exception.getMessage());
    }

    @Test
    void updateUserNullLoginShouldUseExistingValue() {
        User user = new User();
        user.setLogin("validLogin");
        user.setEmail("email@example.com");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userController.createUser(user);

        User updatedData = new User();
        updatedData.setId(createdUser.getId());
        updatedData.setLogin(null); // Null login
        updatedData.setEmail("updated_email@example.com");
        updatedData.setName("Updated Name");
        updatedData.setBirthday(LocalDate.of(1995, 1, 1));

        User updatedUser = userController.updateUser(updatedData);

        assertEquals("validLogin", updatedUser.getLogin()); // Login remains unchanged
        assertEquals("updated_email@example.com", updatedUser.getEmail());
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals(LocalDate.of(1995, 1, 1), updatedUser.getBirthday());
    }

    @Test
    void updateUserNullEmailShouldUseExistingValue() {
        User user = new User();
        user.setLogin("validLogin");
        user.setEmail("email@example.com");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userController.createUser(user);

        User updatedData = new User();
        updatedData.setId(createdUser.getId());
        updatedData.setLogin("updatedLogin");
        updatedData.setEmail(null); // Null email
        updatedData.setName("Updated Name");
        updatedData.setBirthday(LocalDate.of(1995, 1, 1));

        User updatedUser = userController.updateUser(updatedData);

        assertEquals("updatedLogin", updatedUser.getLogin());
        assertEquals("email@example.com", updatedUser.getEmail()); // Email remains unchanged
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals(LocalDate.of(1995, 1, 1), updatedUser.getBirthday());
    }

    @Test
    void updateUserNullBirthdayShouldUseExistingValue() {
        User user = new User();
        user.setLogin("validLogin");
        user.setEmail("email@example.com");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userController.createUser(user);

        User updatedData = new User();
        updatedData.setId(createdUser.getId());
        updatedData.setLogin("updatedLogin");
        updatedData.setEmail("updated_email@example.com");
        updatedData.setName("Updated Name");
        updatedData.setBirthday(null); // Null birthday

        User updatedUser = userController.updateUser(updatedData);

        assertEquals("updatedLogin", updatedUser.getLogin());
        assertEquals("updated_email@example.com", updatedUser.getEmail());
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals(LocalDate.of(2000, 1, 1), updatedUser.getBirthday()); // Birthday remains unchanged
    }

}
