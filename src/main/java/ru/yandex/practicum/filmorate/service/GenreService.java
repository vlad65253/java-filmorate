package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@SuppressWarnings("ALL")
@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

    public List<Genre> getAllGenres() {
        List<Genre> genres = genreRepository.getGenres();
        log.debug("Получено жанров: {}", genres.size());
        return genres;
    }

    public Genre getGenreById(Integer id) {
        return genreRepository.getGenreById(id).get();
    }

    public Genre update(Genre genre) {
        return genreRepository.getGenreById(genre.getId()).get();
    }

//    public void deleteGenre(Integer filmId) {
//        genreRepository.delGenres(filmId);
//        log.info("Удалены жанры для фильма с id: {}", filmId);
//    }
}