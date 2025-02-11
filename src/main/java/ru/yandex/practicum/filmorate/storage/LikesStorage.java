package ru.yandex.practicum.filmorate.storage;

import java.util.Map;
import java.util.Set;

public interface LikesStorage {

    void addLike(int filmId, int userId);

    void deleteLike(int filmId, int userId);

    int getLikeCountForFilm(int filmId);

    Set<Integer> getLikedFilmsByUser(int userId);

    Map<Integer, Long> getCommonLikes(String sql, Object[] params);
}