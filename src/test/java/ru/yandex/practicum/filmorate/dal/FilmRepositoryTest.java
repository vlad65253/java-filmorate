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
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmRepository.class,
        FilmService.class,
        LikesRepository.class,
        LikesRepository.class,
        RatingRepository.class,
        FilmRowMapper.class,
        RatingRowMapper.class,
        GenreRowMapper.class,
        DirectorRowMapper.class,
        UserRowMapper.class,
        GenreRepository.class,
        DirectorRepository.class,
        UserRepository.class,
        EventRepository.class,
        EventRowMapper.class
})
class FilmRepositoryTest {
    private static Film film1;
    private static Film film2;
    private static Film film3;
    private final FilmRepository filmRepository;
    private final LikesRepository likesRepository;
    private final FilmService filmService;

    @BeforeAll
    static void beforeAll() {
        film1 = Film.builder()
                .name("Test Film1")
                .description("Test description2")
                .releaseDate(LocalDate.of(2022, 1, 1))
                .duration(100)
                .mpa(new Rating(1, "G"))
                .genres(Set.of(new Genre(1, "Комедия")))
                .directors(Set.of(new Director(2, "name")))
                .build();

        film2 = Film.builder()
                .name("Test Film2")
                .description("Test description2")
                .releaseDate(LocalDate.of(2022, 1, 1))
                .duration(100)
                .mpa(new Rating(2, "PG"))
                .genres(Set.of(new Genre(2, "Драма")))
                .directors(Set.of(new Director(2, "name")))
                .build();

        film3 = Film.builder()
                .name("Test Film3")
                .description("Test description3")
                .releaseDate(LocalDate.of(2022, 1, 1))
                .duration(100)
                .mpa(new Rating(3, "PG-13"))
                .genres(Set.of(new Genre(6, "Боевик")))
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
        Film film = filmRepository.getFilmById(film1.getId());
        assertThat(film).hasFieldOrPropertyWithValue("id", film1.getId());
    }

    @Test
    void getTopFilms() {
        filmRepository.createFilm(film1);
        filmRepository.createFilm(film2);
        filmRepository.createFilm(film3);
        assertThat(filmService.getTopFilms(10, null, null)).isNotEmpty();
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
                .id(film1.getId())
                .name("Updated Film")
                .description("Updated description")
                .releaseDate(LocalDate.of(2022, 1, 1))
                .duration(100)
                .mpa(new Rating(2, "PG"))
                .genres(Set.of(new Genre(1, "Комедия")))
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

        filmRepository.deleteFilm(film1.getId());
        assertThrows(NotFoundException.class, () -> filmRepository.getFilmById(1));
    }

    @Test
    void getLikedFilmsByUser() {
        Set<Integer> likedFilms = likesRepository.getLikedFilmsByUser(1);
        assertThat(likedFilms).isNotNull();
    }
}