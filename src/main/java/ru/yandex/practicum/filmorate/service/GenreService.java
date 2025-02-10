package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;

@SuppressWarnings("ALL")
@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage genreStorage;

    public List<Genre> getAllGenres() {
        List<Genre> genres = genreStorage.getGenres();
        log.debug("Получено жанров: {}", genres.size());
        return genres;
    }

    public Genre getGenreById(Integer id) {
        return genreStorage.getGenreById(id);
    }

    public Genre update(Genre genre) {
        return genreStorage.getGenreById(genre.getId());
    }
}