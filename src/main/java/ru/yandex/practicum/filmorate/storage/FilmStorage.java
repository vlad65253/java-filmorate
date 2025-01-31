package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import javax.swing.text.html.Option;
import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Film createFilm(Film film);

    Film updateFilm(Film filmUpdated);

    Collection<Film> getFilms();


    Film getFilm(Integer id);

    void deleteFilm(Integer id);

    Collection<Film> getTopFilms(Integer count);

    Collection<Film> getByDirectorId(int directorId, String sortBy);
}
