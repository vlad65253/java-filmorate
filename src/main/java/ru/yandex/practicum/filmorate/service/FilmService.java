package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    static final LocalDate DATE_BIRTHDAY_CINEMA = LocalDate.of(1895, 12, 28);

    public Film createFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации фильма");
            throw new ValidationException("Ошибка валидации фильма.");
        }
        if (film.getDescription() == null || film.getDescription().isBlank() || film.getDescription().length() > 200) {
            log.error("Ошибка валидации описания");
            throw new ValidationException("Ошибка валидации описания.");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(DATE_BIRTHDAY_CINEMA)) {
            log.error("Ошибка валидации даты создания");
            throw new ValidationException("Ошибка валидации даты создания.");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.error("Ошибка валидации продолжительности фильма");
            throw new ValidationException("Ошибка валидации продолжительности фильма.");
        }
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film filmUpdated) {
        Film filmTemp = filmStorage.getFilm(filmUpdated.getId());
        if (filmUpdated.getReleaseDate() == null) {
            filmUpdated.setReleaseDate(filmTemp.getReleaseDate());
        }
        if (filmUpdated.getName() == null) {
            filmUpdated.setName(filmTemp.getName());
        }
        if (filmUpdated.getDescription() == null) {
            filmUpdated.setDescription(filmTemp.getDescription());
        }
        if (filmUpdated.getDuration() == null) {
            filmUpdated.setDuration(filmTemp.getDuration());
        }
        if (filmUpdated.getDescription().length() > 200) {
            log.error("описание > 200");
            throw new ValidationException("Ошибка валидации описания.");
        }
        if (filmUpdated.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("дата релиза позже праздника");
            throw new ValidationException("Ошибка валидации даты создания.");
        }
        if (filmUpdated.getDuration() <= 0) {
            log.error("отрицательная длина фильма");
            throw new ValidationException("Ошибка валидации продолжительности фильма.");
        }
        return filmStorage.updateFilm(filmUpdated);
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilm(long id) {
        return filmStorage.getFilm(id);
    }

    public void deleteFilm(long id) {
        filmStorage.deleteFilm(id);
    }

    public void likeFilm(long filmId, long userId) {
        userStorage.getUserById(userId);// чтобы проверит что такой пользователь есть

        filmStorage.getFilm(filmId).getLikedList().add(userId);
        log.info("к Фильму {} добавился лайк от {}", filmId, userId);
    }

    public void dellikeFilm(long id, long userId) {
        userStorage.getUserById(userId); // также проверка на существующего пользователя

        filmStorage.getFilm(id).getLikedList().remove(userId);
        log.info("Лайк на Фильм {} от пользователя {} убран(", id, userId);
    }

    public Collection<Film> getTopFilms(int count) {
        return filmStorage.getFilms().stream().sorted((film1, film2) -> film2.getLikedList().size() - film1.getLikedList().size()).limit(count).toList();
    }


}
