package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Film createFilm(Film film);

    Film updateFilm(Film filmUpdated);

    Collection<Film> getFilms();

    Film getFilm(Integer id);

    void deleteFilm(Integer id);
    Collection<Film> getTopFilms(Integer count);
}
