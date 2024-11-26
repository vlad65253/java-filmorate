package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController filmController;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
    }

    @Test
    void createFilmValidFilmShouldReturnFilm() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("This is a valid description.");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Film createdFilm = filmController.createFilm(film);

        assertEquals(film.getName(), createdFilm.getName());
        assertEquals(film.getDescription(), createdFilm.getDescription());
        assertEquals(film.getReleaseDate(), createdFilm.getReleaseDate());
        assertEquals(film.getDuration(), createdFilm.getDuration());
    }

    @Test
    void createFilmDescriptionTooLongShouldThrowValidationException() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("A".repeat(201)); // 201 символ
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.createFilm(film));
        assertEquals("Ошибка валидации описания.", exception.getMessage());
    }

    @Test
    void createFilmReleaseDateTooEarlyShouldThrowValidationException() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("This is a valid description.");
        film.setReleaseDate(LocalDate.of(1895, 12, 27)); // Релиз до 28 декабря 1895
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.createFilm(film));
        assertEquals("Ошибка валидации даты создания.", exception.getMessage());
    }

    @Test
    void createFilmNegativeDurationShouldThrowValidationException() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("This is a valid description.");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(-1);

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.createFilm(film));
        assertEquals("Ошибка валидации продолжительности фильма.", exception.getMessage());
    }

    @Test
    void updateFilmValidFilmShouldReturnUpdatedFilm() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("This is a valid description.");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Film createdFilm = filmController.createFilm(film);

        createdFilm.setDescription("Updated description.");
        Film updatedFilm = filmController.updateFilm(createdFilm);

        assertEquals("Updated description.", updatedFilm.getDescription());
    }

    @Test
    void updateFilmNonExistingIdShouldThrowValidationException() {
        Film film = new Film();
        film.setId(999L); // Несуществующий ID
        film.setName("Non-existing Film");
        film.setDescription("This is a valid description.");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.updateFilm(film));
        assertEquals("Фильма с таким айди нет.", exception.getMessage());
    }

    @Test
    void updateFilmDescriptionTooLongShouldThrowValidationException() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("This is a valid description.");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Film createdFilm = filmController.createFilm(film);

        createdFilm.setDescription("A".repeat(201)); // 201 символ
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.updateFilm(createdFilm));
        assertEquals("Ошибка валидации описания.", exception.getMessage());
    }

    @Test
    void getFilmsShouldReturnAllFilms() {
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2020, 1, 1));
        film1.setDuration(120);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2021, 1, 1));
        film2.setDuration(150);

        filmController.createFilm(film1);
        filmController.createFilm(film2);

        Collection<Film> films = filmController.getFilms();

        assertEquals(2, films.size());
        assertTrue(films.contains(film1));
        assertTrue(films.contains(film2));
    }

    @Test
    void createFilmNullReleaseDateShouldThrowValidationException() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("This is a valid description.");
        film.setReleaseDate(null); // Null release date
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.createFilm(film));
        assertEquals("Ошибка валидации даты создания.", exception.getMessage());
    }

    @Test
    void createFilmNullDurationShouldThrowValidationException() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("This is a valid description.");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(null); // Null duration

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.createFilm(film));
        assertEquals("Ошибка валидации продолжительности фильма.", exception.getMessage());
    }

    @Test
    void updateFilmNullNameShouldUseExistingValue() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("This is a valid description.");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Film createdFilm = filmController.createFilm(film);

        Film updateFilm = new Film();
        updateFilm.setId(createdFilm.getId());
        updateFilm.setName(null); // Null name
        updateFilm.setDescription("Updated description.");
        updateFilm.setReleaseDate(LocalDate.of(2021, 1, 1));
        updateFilm.setDuration(150);

        Film updatedFilm = filmController.updateFilm(updateFilm);

        assertEquals("Valid Film", updatedFilm.getName()); // Name remains unchanged
        assertEquals("Updated description.", updatedFilm.getDescription());
        assertEquals(LocalDate.of(2021, 1, 1), updatedFilm.getReleaseDate());
        assertEquals(150, updatedFilm.getDuration());
    }

    @Test
    void updateFilmNullDescriptionShouldUseExistingValue() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("This is a valid description.");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Film createdFilm = filmController.createFilm(film);

        Film updateFilm = new Film();
        updateFilm.setId(createdFilm.getId());
        updateFilm.setName("Updated Name");
        updateFilm.setDescription(null); // Null description
        updateFilm.setReleaseDate(LocalDate.of(2021, 1, 1));
        updateFilm.setDuration(150);

        Film updatedFilm = filmController.updateFilm(updateFilm);

        assertEquals("Updated Name", updatedFilm.getName());
        assertEquals("This is a valid description.", updatedFilm.getDescription()); // Description remains unchanged
        assertEquals(LocalDate.of(2021, 1, 1), updatedFilm.getReleaseDate());
        assertEquals(150, updatedFilm.getDuration());
    }

    @Test
    void updateFilmNullReleaseDateShouldUseExistingValue() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("This is a valid description.");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Film createdFilm = filmController.createFilm(film);

        Film updateFilm = new Film();
        updateFilm.setId(createdFilm.getId());
        updateFilm.setName("Updated Name");
        updateFilm.setDescription("Updated description.");
        updateFilm.setReleaseDate(null); // Null release date
        updateFilm.setDuration(150);

        Film updatedFilm = filmController.updateFilm(updateFilm);

        assertEquals("Updated Name", updatedFilm.getName());
        assertEquals("Updated description.", updatedFilm.getDescription());
        assertEquals(LocalDate.of(2020, 1, 1), updatedFilm.getReleaseDate()); // Release date remains unchanged
        assertEquals(150, updatedFilm.getDuration());
    }

    @Test
    void updateFilmNullDurationShouldUseExistingValue() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("This is a valid description.");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Film createdFilm = filmController.createFilm(film);

        Film updateFilm = new Film();
        updateFilm.setId(createdFilm.getId());
        updateFilm.setName("Updated Name");
        updateFilm.setDescription("Updated description.");
        updateFilm.setReleaseDate(LocalDate.of(2021, 1, 1));
        updateFilm.setDuration(null); // Null duration

        Film updatedFilm = filmController.updateFilm(updateFilm);

        assertEquals("Updated Name", updatedFilm.getName());
        assertEquals("Updated description.", updatedFilm.getDescription());
        assertEquals(LocalDate.of(2021, 1, 1), updatedFilm.getReleaseDate());
        assertEquals(120, updatedFilm.getDuration()); // Duration remains unchanged
    }

}
