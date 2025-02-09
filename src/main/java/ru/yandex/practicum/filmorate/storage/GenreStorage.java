package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

public interface GenreStorage {

    List<Genre> getGenres();

    Optional<Genre> getGenreById(long id);

    void createGenresForFilmById(long filmId, List<Genre> genres);

    void deleteGenreForFilmById(long id);

    Set<Genre> getGenresFilmById(long id);
}