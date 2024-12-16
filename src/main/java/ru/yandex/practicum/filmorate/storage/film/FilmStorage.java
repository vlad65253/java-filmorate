package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Film createFilm(Film film);

    Film updateFilm(Film filmUpdated);

    Collection<Film> getFilms();

    Film getFilm(long id);

    void deleteFilm(long id);
}
