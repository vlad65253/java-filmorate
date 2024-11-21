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

        assertNotNull(createdFilm.getId());
        assertEquals(film.getName(), createdFilm.getName());
        assertEquals(film.getDescription(), createdFilm.getDescription());
        assertEquals(film.getReleaseDate(), createdFilm.getReleaseDate());
        assertEquals(film.getDuration(), createdFilm.getDuration());
    }

    @Test
    void createFilmEmptyNameShouldThrowValidationException() {
        Film film = new Film();
        film.setName("");
        film.setDescription("This is a valid description.");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }

    @Test
    void createFilmDescriptionTooLongShouldThrowValidationException() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("A".repeat(201)); // 201 символ
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }

    @Test
    void createFilmReleaseDateTooEarlyShouldThrowValidationException() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("This is a valid description.");
        film.setReleaseDate(LocalDate.of(1895, 12, 27)); // Релиз до 28 декабря 1895
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }

    @Test
    void createFilmNegativeDurationShouldThrowValidationException() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("This is a valid description.");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(-1);

        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
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

        assertThrows(ValidationException.class, () -> filmController.updateFilm(film));
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
        assertThrows(ValidationException.class, () -> filmController.updateFilm(createdFilm));
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
    }
}
