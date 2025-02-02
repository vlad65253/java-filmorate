package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Set;

public interface FilmStorage {
    Film createFilm(Film film);

    Film updateFilm(Film filmUpdated);

    Collection<Film> getFilms();

    Film getFilm(Integer id);

    void deleteFilm(Integer id);

    Collection<Film> getTopFilms(Integer count);

    Collection<Film> getByDirectorId(int directorId, String sortBy);

    boolean ratingExists(Integer id);

    boolean genreTry(Integer id);

    public Collection<Film> getCommonFilms(int userId, int friendId);
}
