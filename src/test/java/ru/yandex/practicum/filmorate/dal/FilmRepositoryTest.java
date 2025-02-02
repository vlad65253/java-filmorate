package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmRepository.class,
        FilmRowMapper.class,
        LikesRepository.class})
class FilmRepositoryTest {
    private final FilmRepository filmRepository;
    private static Film film1;
    private static Film film2;
    private static Film film3;
    private final LikesRepository likesRepository;

    @BeforeAll
    static void beforeAll() {
        film1 = Film.builder()
                .name("Test Film1")
                .description("Test description2")
                .releaseDate(LocalDate.of(2022, 1, 1))
                .duration(100)
                .mpa(new Rating(1, "G"))
                .genres(Set.of(new Genre(1, "Комедия")))
                .likedList(Set.of(15, 2, 7, 3))
                .directors(Set.of(new Director(2, "name")))
                .build();

        film2 = Film.builder()
                .name("Test Film2")
                .description("Test description2")
                .releaseDate(LocalDate.of(2022, 1, 1))
                .duration(100)
                .mpa(new Rating(2, "PG"))
                .genres(Set.of(new Genre(2, "Драма")))
                .likedList(Set.of(1, 2, 7, 3))
                .directors(Set.of(new Director(2, "name")))
                .build();

        film3 = Film.builder()
                .name("Test Film3")
                .description("Test description3")
                .releaseDate(LocalDate.of(2022, 1, 1))
                .duration(100)
                .mpa(new Rating(3, "PG-13"))
                .genres(Set.of(new Genre(6, "Боевик")))
                .likedList(Set.of(1, 15, 2, 7, 3))
                .directors(Set.of(new Director(2, "name")))
                .build();
    }

    @Test
    void getFilms() {
        filmRepository.createFilm(film1);
        filmRepository.createFilm(film2);
        filmRepository.createFilm(film3);
        assertThat(filmRepository.getFilms()).isNotEmpty();
    }

    @Test
    void getFilmById() {
        filmRepository.createFilm(film1);
        Film film = filmRepository.getFilm(film1.getId());
        assertThat(film).hasFieldOrPropertyWithValue("id", 13);
    }

    @Test
    void getTopFilms() {
        filmRepository.createFilm(film1);
        filmRepository.createFilm(film2);
        filmRepository.createFilm(film3);
        assertThat(filmRepository.getTopFilms(10)).isNotEmpty();
        //Это доделать
    }

    @Test
    void create() {
        filmRepository.createFilm(film1);
        filmRepository.createFilm(film2);
        filmRepository.createFilm(film3);

        assertThat(film2).hasFieldOrPropertyWithValue("id", 2);
    }

    @Test
    void update() {
        filmRepository.createFilm(film1);
        filmRepository.createFilm(film2);
        filmRepository.createFilm(film3);
        Film updatedFilm = Film.builder()
                .id(1)
                .name("Updated Film")
                .description("Updated description")
                .releaseDate(LocalDate.of(2022, 1, 1))
                .duration(100)
                .mpa(new Rating(2, "PG"))
                .genres(Set.of(new Genre(1, "Комедия")))
                .likedList(Set.of())
                .directors(Set.of(new Director(2, "name")))
                .build();

        Film updated = filmRepository.updateFilm(updatedFilm);
        assertThat(updated).hasFieldOrPropertyWithValue("name", "Updated Film");
    }

    @Test
    void delete() {
        filmRepository.createFilm(film1);
        filmRepository.createFilm(film2);
        filmRepository.createFilm(film3);

        filmRepository.deleteFilm(1);
        assertThrows(NotFoundException.class, () -> filmRepository.getFilm(1));
    }
}