package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dal.LikesRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmRepository filmRepository;
    private final UserStorage userStorage;
    private final GenreRepository genreRepository;
    private final LikesRepository likesRepository;
    private final DirectorRepository directorRepository;

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
        if (!filmRepository.ratingExists(film.getMpa().getId())) {
            throw new NotFoundException("Рейтинг МПА с ID " + film.getMpa().getId() + " не найден");
        }
        // Создаем фильм
        Film createdFilm = filmRepository.createFilm(film);
        log.info("Создан фильм: {}", createdFilm);
        // Если жанры заданы, сохраняем их
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreRepository.addGenres(createdFilm, film.getGenres().stream().toList());
        }
        // Если режиссёры заданы, сохраняем их
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            directorRepository.addDirector(createdFilm.getId(),
                    film.getDirectors().stream().map(Director::getId).toList());
        }
        return createdFilm;
    }

    public Film updateFilm(Film filmUpdated) {
        if (filmUpdated.getId() == null) {
            throw new NotFoundException("ID фильма не передан.");
        }
        // Проверяем, что фильм существует
        Film savedFilm = filmRepository.getFilm(filmUpdated.getId());
        // Проверяем наличие рейтинга МПА
        if (!filmRepository.ratingExists(filmUpdated.getMpa().getId())) {
            throw new NotFoundException("Рейтинг МПА с ID " + filmUpdated.getMpa().getId() + " не найден");
        }
        // Обновляем связи с жанрами: удаляем старые и добавляем новые
        if (savedFilm.getGenres() != null) {
            genreRepository.delGenres(savedFilm.getId());
        }
        if (filmUpdated.getGenres() != null && !filmUpdated.getGenres().isEmpty()) {
            genreRepository.addGenres(savedFilm, filmUpdated.getGenres().stream().toList());
        }
        // Обновляем связи с режиссёрами: удаляем старые и добавляем новые
        if (savedFilm.getDirectors() != null) {
            directorRepository.delDirector(savedFilm.getId());
        }
        if (filmUpdated.getDirectors() != null && !filmUpdated.getDirectors().isEmpty()) {
            directorRepository.addDirector(savedFilm.getId(),
                    filmUpdated.getDirectors().stream().map(Director::getId).toList());
        }
        Film updatedFilm = filmRepository.updateFilm(filmUpdated);
        log.info("Обновлён фильм: {}", updatedFilm);
        return updatedFilm;
    }

    public Collection<Film> getFilms() {
        Collection<Film> films = filmRepository.getFilms();
        log.debug("Получено фильмов: {}", films.size());
        return films;
    }

    public Film getFilm(Integer id) {
        Film film = filmRepository.getFilm(id);
        log.debug("Получен фильм: {}", film);
        return film;
    }

    public void deleteFilm(Integer id) {
        filmRepository.deleteFilm(id);
        log.info("Удалён фильм с id: {}", id);
    }

    public Collection<Film> getTopFilms(int count) {
        Collection<Film> films = filmRepository.getTopFilms(count);
        log.debug("Получены топ {} фильмов", count);
        return films;
    }

    public Collection<Film> getTopFilmsByGenreAndYear(int count, Integer genreId, Integer year) {
        Collection<Film> films = filmRepository.getTopFilmsByGenreAndYear(count, genreId, year);
        log.debug("Получены топ фильмы с фильтрами: genreId={}, year={}", genreId, year);
        return films;
    }

    public Collection<Film> getCommonFilms(int userId, int friendId) {
        Collection<Film> films = filmRepository.getCommonFilms(userId, friendId);
        log.debug("Получены общие фильмы для пользователей {} и {}", userId, friendId);
        return films;
    }

    public Collection<Film> getFilmsByDirector(int directorId, String sortBy) {
        if (directorRepository.getDirectorById(directorId) == null) {
            throw new NotFoundException("Режиссёр с ID " + directorId + " не найден");
        }
        Collection<Film> films = filmRepository.getByDirectorId(directorId, sortBy);
        log.debug("Получены фильмы режиссёра {} с сортировкой: {}", directorId, sortBy);
        return films;
    }

    public Collection<Film> getSearchFilms(String query, String by) {
        Collection<Film> films = filmRepository.getSearchFilms(query, by);
        log.debug("Результаты поиска по query={} и by={}: {}", query, by, films.size());
        return films;
    }

    public Set<Integer> getLikedFilmsByUser(int userId) {
        Set<Integer> likedFilms = filmRepository.getLikedFilmsByUser(userId);
        log.debug("Пользователь {} лайкнул фильмы: {}", userId, likedFilms);
        return likedFilms;
    }

    /**
     * Добавляет лайк фильму от пользователя.
     * Проверяет наличие фильма и пользователя.
     */
    public Film likeFilm(Integer filmId, Integer userId) {
        // Проверяем наличие фильма и пользователя
        Film film = filmRepository.getFilm(filmId);
        userStorage.getUserById(userId);
        likesRepository.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        return filmRepository.getFilm(filmId);
    }

    /**
     * Удаляет лайк у фильма от пользователя.
     * Проверяет наличие фильма и пользователя.
     */
    public void delLikeFilm(Integer filmId, Integer userId) {
        filmRepository.getFilm(filmId);
        userStorage.getUserById(userId);
        likesRepository.deleteLike(filmId, userId);
        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }
}