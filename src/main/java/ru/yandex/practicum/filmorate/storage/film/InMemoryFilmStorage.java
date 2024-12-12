package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    static final LocalDate DATE_BIRTHDAY_CINEMA = LocalDate.of(1895, 12, 28);
    @Override
    public Film createFilm(Film film){
        long filmId = nextId();
        film.setId(filmId);
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
        films.put(filmId, film);
        log.info("Новый фильм создан");
        return film;
    }
    @Override
    public Film updateFilm(Film filmUpdated){
        Film filmTemp = films.get(filmUpdated.getId());
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
        if (!films.containsKey(filmUpdated.getId())) {
            throw new NotFoundException("Фильма с таким айди нет.");
        }
        films.put(filmUpdated.getId(), filmUpdated);
        log.info("Фильм изменен");
        return filmUpdated;
    }
    @Override
    public Collection<Film> getFilms(){
        return films.values();

    }
    @Override
    public Film getFilm(long id){
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с указанным id не найден");
        }
        return films.get(id);
    }
    @Override
    public void deleteFilm(long id){
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
