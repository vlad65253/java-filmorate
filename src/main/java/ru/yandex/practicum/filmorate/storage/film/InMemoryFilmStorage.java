package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film createFilm(Film film) {
        long filmId = nextId();
        film.setId(filmId);
        films.put(filmId, film);
        log.info("Новый фильм создан");
        return film;
    }

    @Override
    public Film updateFilm(Film filmUpdated) {
        films.put(filmUpdated.getId(), filmUpdated);
        log.info("Фильм изменен");
        return filmUpdated;
    }

    @Override
    public Collection<Film> getFilms() {
        return films.values();

    }

    @Override
    public Film getFilm(long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с указанным id не найден");
        }
        return films.get(id);
    }

    @Override
    public void deleteFilm(long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с указанным id не найден");
        }
        films.remove(id);
        log.info("Удалили фильм {}", id);
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
