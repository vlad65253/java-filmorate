package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        long filmId = nextId();
        film.setId(filmId);
        if (film.getName().isEmpty()) {
            throw new ValidationException("У фильма должно быть имя.");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания - 200 символов.");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
        films.put(filmId, film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film filmUpdated) {
        if (filmUpdated.getDescription().isEmpty()) {
            throw new ValidationException("У фильма должно быть имя.");
        }
        if (filmUpdated.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания - 200 символов.");
        }
        if (!films.containsKey(filmUpdated.getId())) {
            throw new ValidationException("Фильма с таким айди нет.");
        }
        films.put(filmUpdated.getId(), filmUpdated);
        return filmUpdated;
    }

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    private long nextId() {
        long nowMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++nowMaxId;
    }
}