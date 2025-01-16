package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dal.LikesRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreRepository genreRepository;
    private final LikesRepository likesRepository;

    public FilmService(@Autowired @Qualifier("filmRepository") FilmStorage filmStorage,
                       @Autowired @Qualifier("userRepository") UserStorage userStorage,
                       @Autowired GenreRepository genreRepository,
                       @Autowired LikesRepository likesRepository) {
        this.filmStorage = filmStorage;
        this.genreRepository = genreRepository;
        this.likesRepository = likesRepository;
        this.userStorage = userStorage;
    }


    public Film createFilm(Film film) {
        Film createdFilm = filmStorage.createFilm(film);
        if (!createdFilm.getGenres().isEmpty()) {
            genreRepository.addGenres(createdFilm.getId(), createdFilm.getGenres()
                    .stream()
                    .map(Genre::getId)
                    .toList());
        }
        return createdFilm;
    }

    public Film updateFilm(Film filmUpdated) {
        if (filmStorage.getFilm(filmUpdated.getId()) == null) {
            throw new NotFoundException("Не передан идентификатор фильма");
        }
        Film updatedFilm = filmStorage.updateFilm(filmUpdated);
        if (!updatedFilm.getGenres().isEmpty()) {
            genreRepository.delGenres(updatedFilm.getId());
            genreRepository.addGenres(updatedFilm.getId(), updatedFilm.getGenres()
                    .stream()
                    .map(Genre::getId)
                    .toList());
        }
        return updatedFilm;
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilm(Integer id) {
        return filmStorage.getFilm(id);
    }

    public void deleteFilm(Integer id) {
        filmStorage.deleteFilm(id);
    }

    public Film likeFilm(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilm(filmId);
        likesRepository.addLike(filmId, userId);
        film.getLikedList().add(userId);
        log.info("User {} liked film {}", userId, filmId);
        return film;
    }

    public void dellikeFilm(Integer id, Integer userId) {
        userStorage.getUserById(userId); // также проверка на существующего пользователя

        likesRepository.deleteLike(id, userId);
        filmStorage.getFilm(id).getLikedList().remove(userId);
        log.info("Лайк на Фильм {} от пользователя {} убран(", id, userId);
    }

    public Collection<Film> getTopFilms(Integer count) {
        return filmStorage.getTopFilms(count);
    }


}
