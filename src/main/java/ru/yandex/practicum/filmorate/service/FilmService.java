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
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film createFilm(Film film){
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film filmUpdated){
        return filmStorage.updateFilm(filmUpdated);
    }
    public Collection<Film> getFilms(){
        return filmStorage.getFilms();
    }
    public Film getFilm(long id){
        return filmStorage.getFilm(id);
    }
    public void deleteFilm(long id){
        filmStorage.deleteFilm(id);
    }
    public void likeFilm(long filmId, long userId){
        userStorage.getUserById(userId);// чтобы проверит что такой пользователь есть

        filmStorage.getFilm(filmId).getLikedList().add(userId);
        log.info("к Фильму {} добавился лайк от {}", filmId, userId);
    }
    public void dellikeFilm(long id, long userId){
        userStorage.getUserById(userId); // также проверка на существующего пользователя

        filmStorage.getFilm(id).getLikedList().remove(userId);
        log.info("Лайк на Фильм {} от пользователя {} убран(", id, userId);
    }
    public Collection<Film> getTopFilms(int count){
        return filmStorage.getFilms().stream()
                .sorted((film1, film2) -> film2.getLikedList().size() - film1.getLikedList().size())
                .limit(count)
                .toList();
    }


}
