package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

public interface GenreStorage {

    List<Genre> getGenres();

    Optional<Genre> getGenreById(int id);

    void createGenresForFilmById(int filmId, List<Genre> genres);

    void deleteGenreForFilmById(int id);

    Set<Genre> getGenresFilmById(int id);
}