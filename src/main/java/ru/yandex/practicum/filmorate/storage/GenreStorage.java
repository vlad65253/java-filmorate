package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface GenreStorage {
    /**
     * Возвращает коллекцию всех жанров.
     */
    Collection<Genre> getAllGenres();

    /**
     * Возвращает жанр по его ID.
     *
     * @param id идентификатор жанра
     * @return жанр
     */
    Genre getGenreById(Integer id);

    /**
     * Добавляет для указанного фильма список жанров.
     *
     * @param film   фильм, к которому добавляются жанры
     * @param genres список жанров
     */
    void addGenres(Film film, List<Genre> genres);

    /**
     * Удаляет все жанры, связанные с указанным фильмом.
     *
     * @param filmId идентификатор фильма
     */
    void delGenres(Integer filmId);

    /**
     * Обновляет данные жанра.
     *
     * @param genre жанр с новыми данными
     * @return обновлённый жанр
     */
    Genre updateGenre(Genre genre);

    /**
     * Возвращает набор жанров для конкретного фильма.
     *
     * @param filmId идентификатор фильма
     * @return набор жанров
     */
    Set<Genre> getGenresByFilm(Integer filmId);

    /**
     * (Опционально) Возвращает набор жанров по всем фильмам.
     *
     * @return набор жанров, сгруппированных по фильмам
     */
    Set<Genre> getAllByFilms();
}