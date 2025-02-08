package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

    public Collection<Genre> getAllGenres() {
        Collection<Genre> genres = genreRepository.getAllGenres();
        log.debug("Получено жанров: {}", genres.size());
        return genres;
    }

    public Genre getGenreById(Integer id) {
        Genre genre = genreRepository.getGenreById(id);
        log.debug("Получен жанр с id: {}", id);
        return genre;
    }

    public Genre update(Genre genre) {
        Genre savedGenre = genreRepository.getGenreById(genre.getId());
        if (genre.getName() != null && !genre.getName().isBlank()) {
            savedGenre.setName(genre.getName());
        } else {
            throw new IllegalArgumentException("Название жанра не может быть пустым");
        }
        //genreRepository.updateGenre(savedGenre);
        log.info("Обновлён жанр с ID {}", genre.getId());
        return savedGenre;
    }

    public void deleteGenre(Integer filmId) {
        genreRepository.delGenres(filmId);
        log.info("Удалены жанры для фильма с id: {}", filmId);
    }
}