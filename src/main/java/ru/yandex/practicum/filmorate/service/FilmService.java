package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final LikesStorage likesStorage;
    private final DirectorStorage directorStorage;
    private final RatingStorage ratingStorage;

    public Film createFilm(Film film) {
        // Проверка входных данных
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым.");
        }
        if (film.getDescription() == null || film.getDescription().isBlank() || film.getDescription().length() > 200) {
            throw new ValidationException("Описание фильма не может быть пустым или превышать 200 символов.");
        }
        if (film.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза не может быть пустой.");
        }
        // Проверяем существование рейтинга МПА
        if (!filmStorage.ratingExists(film.getMpa().getId())) {
            throw new NotFoundException("Рейтинг МПА с ID " + film.getMpa().getId() + " не найден");
        }
        // Создаем фильм
        Film createdFilm = filmStorage.createFilm(film);
        log.info("Создан фильм: {}", createdFilm);
        // Если жанры заданы, сохраняем их
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreStorage.createGenresForFilmById(film.getId(), film.getGenres().stream().toList());
            film.setGenres(genreStorage.getGenresFilmById(film.getId()));
        }
        // Если режиссёры заданы, сохраняем их
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            directorStorage.createDirectorsForFilmById(film.getId(), film.getDirectors().stream().toList());
            film.setDirectors(directorStorage.getDirectorsFilmById(film.getId()));
        }
        return createdFilm;
    }

//    public Film updateFilm(Film filmUpdated) {
//        if (filmUpdated.getId() == null) {
//            throw new NotFoundException("ID фильма не передан.");
//        }
//        // Проверяем, что фильм существует
//        Film savedFilm = filmStorage.getFilmById(filmUpdated.getId()).get();
//        // Проверяем наличие рейтинга МПА
//        if (!filmStorage.ratingExists(filmUpdated.getMpa().getId())) {
//            throw new NotFoundException("Рейтинг МПА с ID " + filmUpdated.getMpa().getId() + " не найден");
//        }
//        // Обновляем связи с жанрами: удаляем старые и добавляем новые
//        if (savedFilm.getGenres() != null) {
//            genreStorage.delGenres(savedFilm.getId());
//        }
//        if (filmUpdated.getGenres() != null && !filmUpdated.getGenres().isEmpty()) {
//            genreStorage.addGenres(savedFilm, filmUpdated.getGenres().stream().toList());
//        }
//        // Обновляем связи с режиссёрами: удаляем старые и добавляем новые
//        if (savedFilm.getDirectors() != null) {
//            directorServis.delDirector(savedFilm.getId());
//        }
//        if (filmUpdated.getDirectors() != null && !filmUpdated.getDirectors().isEmpty()) {
//            directorServis.addDirector(savedFilm.getId(),
//                    filmUpdated.getDirectors().stream().map(Director::getId).toList());
//        }
//        Film updatedFilm = filmStorage.updateFilm(filmUpdated);
//        log.info("Обновлён фильм: {}", updatedFilm);
//        return updatedFilm;
//    }

    public Set<Film> getFilms() {
        List<Film> filmList = filmStorage.getFilms();
        for (Film film : filmList) {
            film.setMpa(ratingStorage.getRatingById(film.getId()).get());
            film.setGenres(genreStorage.getGenresFilmById(film.getId()));
            film.setDirectors(directorStorage.getDirectorsFilmById(film.getId()));
        }
        return filmList.stream()
                .sorted(Comparator.comparing(Film::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Film getFilmById(Integer id) {
        Film film = filmStorage.getFilmById(id).get();
        log.debug("Получен фильм: {}", film);
        return film;
    }

    public void deleteFilm(Integer id) {
        filmStorage.deleteFilm(id);
        log.info("Удалён фильм с id: {}", id);
    }

    public Collection<Film> getTopFilms(int count) {
        Collection<Film> films = filmStorage.getTopFilms(count);
        log.debug("Получены топ {} фильмов", count);
        return films;
    }

    public Collection<Film> getCommonFilms(int userId, int friendId) {
        Collection<Film> films = filmStorage.getCommonFilms(userId, friendId);
        log.debug("Получены общие фильмы для пользователей {} и {}", userId, friendId);
        return films;
    }

//    public Collection<Film> getFilmsByDirector(int directorId, String sortBy) {
//        if (directorStorage.getDirectorById(directorId) == null) {
//            throw new NotFoundException("Режиссёр с ID " + directorId + " не найден");
//        }
//        Collection<Film> films = filmStorage.getByDirectorId(directorId, sortBy);
//        log.debug("Получены фильмы режиссёра {} с сортировкой: {}", directorId, sortBy);
//        return films;
//    }

//    public Collection<Film> getSearchFilms(String query, String by) {
//        Collection<Film> films = filmStorage.getSearchFilms(query, by);
//        log.debug("Результаты поиска по query={} и by={}: {}", query, by, films.size());
//        return films;
//    }

//    public Set<Integer> getLikedFilmsByUser(int userId) {
//        Set<Integer> likedFilms = filmStorage.getLikedFilmsByUser(userId);
//        log.debug("Пользователь {} лайкнул фильмы: {}", userId, likedFilms);
//        return likedFilms;
//    }

    public Film likeFilm(Integer filmId, Integer userId) {
        // Проверяем наличие фильма и пользователя
        Film film = filmStorage.getFilmById(filmId).get();
        userStorage.getUserById(userId);
        likesStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        return film;
    }

    public void delLikeFilm(Integer filmId, Integer userId) {
        filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);
        likesStorage.deleteLike(filmId, userId);
        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }
}