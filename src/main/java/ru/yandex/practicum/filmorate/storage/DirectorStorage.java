package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface DirectorStorage {
    Collection<Director> getAllDirectors();

    Director getDirectorById(Integer id);

    void addDirector(Integer filmId, List<Integer> directorId);

    void delDirector(long id);

    boolean directorExists(Integer directorId);

    Director createDirector(Director director);

    Director updateDirector(Director director);

    Collection<Director> getAllDirectorsByFilmId(Integer id);

    Map<Integer, List<Director>> findAllByFilms();
}