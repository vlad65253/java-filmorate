package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Rating;
import java.util.Collection;

/**
 * Интерфейс для операций с рейтингами (MPA).
 */
public interface RatingStorage {

    /**
     * Возвращает коллекцию всех рейтингов.
     *
     * @return коллекция рейтингов
     */
    Collection<Rating> getAllRatings();

    /**
     * Возвращает рейтинг по его ID.
     *
     * @param id идентификатор рейтинга
     * @return рейтинг
     */
    Rating getRatingById(Integer id);
}