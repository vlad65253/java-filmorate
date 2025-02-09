package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {
    Film createFilm(Film film);

    Film updateFilm(Film filmUpdated);

    List<Film> getFilms();

    Optional<Film> getFilmById(Integer id);

    void deleteFilm(Integer id);

    Set<Film> getTopFilms();

//    Collection<Film> getByDirectorId(int directorId, String sortBy);

    boolean ratingExists(Integer id);

    Collection<Film> getCommonFilms(int userId, int friendId);

//    Collection<Film> getSearchFilms(String query, String by);
}
