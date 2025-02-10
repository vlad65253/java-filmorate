package ru.yandex.practicum.filmorate.storage;

import java.util.Map;
import java.util.Set;

public interface LikesStorage {

    void addLike(Integer filmId, Integer userId);

    void deleteLike(Integer filmId, Integer userId);

    int getLikeCountForFilm(Integer filmId);

    Set<Integer> getLikedFilmsByUser(Integer userId);

    public Map<Integer, Long> getCommonLikes(String sql, Object[] params);
}