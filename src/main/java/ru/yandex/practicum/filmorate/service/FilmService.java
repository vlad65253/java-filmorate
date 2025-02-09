package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
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
        if(film.getGenres() != null) {
            film.getGenres().forEach(g -> genreStorage.getGenreById(g.getId()));
        }
        // Создаем фильм
        Film createdFilm = filmStorage.createFilm(film);
        film.setMpa(ratingStorage.getRatingById(film.getMpa().getId()).get());

        if (film.getGenres() != null) {
            genreStorage.createGenresForFilmById(film.getId(), film.getGenres().stream().toList());
            film.setGenres(genreStorage.getGenresFilmById(film.getId()));
        }

        if (film.getDirectors() != null) {
            directorStorage.createDirectorsForFilmById(film.getId(), film.getDirectors().stream().toList());
            film.setDirectors(directorStorage.getDirectorsFilmById(film.getId()));
        }
        return createdFilm;
    }

    public Film updateFilm(Film film) {

        // Проверка входных данных
        filmStorage.getFilmById(film.getId());
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
        film.getGenres().forEach(g -> genreStorage.getGenreById(g.getId()));
        // Обновление фильма
        Film createdFilm = filmStorage.updateFilm(film);
        film.setMpa(ratingStorage.getRatingById(film.getMpa().getId()).get());

        if (film.getGenres() != null) {
            genreStorage.deleteGenreForFilmById(film.getId());
            genreStorage.createGenresForFilmById(film.getId(), film.getGenres().stream().toList());
            film.setGenres(genreStorage.getGenresFilmById(film.getId()));
        }

        if (film.getDirectors() != null) {
            directorStorage.deleteDirectorsFilmById(film.getId());
            directorStorage.createDirectorsForFilmById(film.getId(), film.getDirectors().stream().toList());
            film.setDirectors(directorStorage.getDirectorsFilmById(film.getId()));
        }
        return film;
    }

    public Set<Film> getFilms() {
        return filmStorage.getFilms().stream()
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

    public Set<Film> getTopFilms(Integer count, Integer genreId, Integer year) {
        if (genreId != null && year != null) {
            return filmStorage.getTopFilms().stream()
                    .filter(f -> f.getGenres().stream()
                            .map(Genre::getId)
                            .anyMatch(i -> genreId == i))
                    .filter(y -> y.getReleaseDate().getYear() == year)
                    .limit(count)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } else if (genreId != null) {
            return filmStorage.getTopFilms().stream()
                    .filter(f -> f.getGenres().stream()
                            .map(Genre::getId)
                            .anyMatch(i -> genreId == i))
                    .limit(count)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } else if (year != null) {
            return filmStorage.getTopFilms().stream()
                    .filter(y -> y.getReleaseDate().getYear() == year)
                    .limit(count)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } else {
            return filmStorage.getTopFilms();
        }
    }


//    public Collection<Film> getCommonFilms(int userId, int friendId) {
//        Collection<Film> films = filmStorage.getCommonFilms(userId, friendId);
//        log.debug("Получены общие фильмы для пользователей {} и {}", userId, friendId);
//        return films;
//    }

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

//    public Film likeFilm(Integer filmId, Integer userId) {
//        // Проверяем наличие фильма и пользователя
//        Film film = filmStorage.getFilmById(filmId).get();
//        userStorage.getUserById(userId);
//        likesStorage.addLike(filmId, userId);
//        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
//        return film;
//    }
//
//    public void delLikeFilm(Integer filmId, Integer userId) {
//        filmStorage.getFilmById(filmId);
//        userStorage.getUserById(userId);
//        likesStorage.deleteLike(filmId, userId);
//        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
}
