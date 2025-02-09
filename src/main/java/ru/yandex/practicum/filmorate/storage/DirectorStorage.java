package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.*;
import java.util.stream.Collectors;

public interface DirectorStorage {
//    Collection<Director> getAllDirectors();
//
//    Director getDirectorById(Integer id);
//
//    void addDirector(Integer filmId, List<Integer> directorId);
//
//    void delDirector(Integer id);
//
//    void delDirectorTable(Integer directorId);
//
//    Director createDirector(Director director);
//
//    Director updateDirector(Director director);
//
//    Collection<Director> getAllDirectorsByFilmId(Integer id);
//
//    Map<Integer, List<Director>> findAllByFilms();
//
//    List<Director> getDirectorsByFilm(int filmId);


    List<Director> getDirectors();

    Optional<Director> getDirectorById(int id);

    Director createDirector(Director director);

    Director updateDirector(Director director);

    void deleteDirectorById(int id);

    void createDirectorsForFilmById(int filmId, List<Director> directors);

    Set<Director> getDirectorsFilmById(int filmId);

    void deleteDirectorsFilmById(int filmId);
}