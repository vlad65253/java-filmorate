package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    @PostMapping
    public Film createFilm(@RequestBody Film film){
        long filmId = nextId();
        films.put(filmId, film);
        return film;
    }
    @PostMapping
    public Film updateFilm(@RequestBody Film filmUpdated){
        if(filmUpdated.getDescription() == null){
            throw new ConditionsNotMetException("У фильма должно быть имя.");
        }
        if(filmUpdated.getDescription().length() > 200){
            throw new ConditionsNotMetException("Максимальная длина описания - 200 символов.");
        }
        if(!films.containsKey(filmUpdated.getId())){
            throw new NotFoundException("Фильма с таким айди нет.");
        }
        films.put(filmUpdated.getId(), filmUpdated);
        return filmUpdated;
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
//осталось дату что с 28 декабря и продолжительность фильма