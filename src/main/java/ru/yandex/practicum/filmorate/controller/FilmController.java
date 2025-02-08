package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.enums.FilmFilters;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@Slf4j
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        return filmService.createFilm(film);
    }

//    @PutMapping
//    public Film updateFilm(@RequestBody Film filmUpdated) {
//        return filmService.updateFilm(filmUpdated);
//    }

    @PutMapping("/{id}/like/{userId}")
    public Film likingFilm(@PathVariable int id, @PathVariable int userId) {
        return filmService.likeFilm(id, userId);
    }

    @GetMapping
    public Collection<Film> getFilms() {
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable int id) {
        return filmService.getFilmById(id);
    }

//    @GetMapping("/popular")
//    public Collection<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
//        return filmService.getTopFilms(count);
//    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable int id, @PathVariable int userId) {
        filmService.delLikeFilm(id, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFilm(@PathVariable int id) {
        filmService.deleteFilm(id);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getFilmsByDirector(@PathVariable int directorId,
                                               @RequestParam(defaultValue = "year") String sortBy) {
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @GetMapping("/common")
    public Collection<Film> getCommonFilms(@RequestParam int userId,
                                           @RequestParam int friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

//    @GetMapping("/popular")
//    public Collection<Film> getPopularFilms(
//            @RequestParam(defaultValue = "10") int count,
//            @RequestParam(required = false) Integer genreId,
//            @RequestParam(required = false) Integer year) {
//        return filmService.getTopFilmsByGenreAndYear(count, genreId, year);
//    }

    @GetMapping("/search")
    public Collection<Film> getSearchFilms(@RequestParam String query,
                                           @RequestParam String by) {
        if (FilmFilters.from(by) == null) {
            throw new ValidationException("Указано неверное значение критерия поиска (by)");
        }
        return filmService.getSearchFilms(query, by);
    }
}