package ru.yandex.practicum.filmorate.storage;

/**
 * Интерфейс для операций с лайками фильмов.
 */
public interface LikesStorage {

    /**
     * Добавляет лайк фильму от пользователя.
     *
     * @param filmId идентификатор фильма
     * @param userId идентификатор пользователя
     */
    void addLike(Integer filmId, Integer userId);

    /**
     * Удаляет лайк у фильма от пользователя.
     *
     * @param filmId идентификатор фильма
     * @param userId идентификатор пользователя
     */
    void deleteLike(Integer filmId, Integer userId);
}