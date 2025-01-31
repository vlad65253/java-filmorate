package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dal.LikesRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.SQLOutput;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreRepository genreRepository;
    private final LikesRepository likesRepository;
    private final FilmRepository filmRepository;
    private final DirectorRepository directorRepository;


    public FilmService(@Autowired @Qualifier("filmRepository") FilmStorage filmStorage,
                       @Autowired @Qualifier("userRepository") UserStorage userStorage,
                       @Autowired GenreRepository genreRepository,
                       @Autowired LikesRepository likesRepository,
                       @Autowired FilmRepository filmRepository,
                       @Autowired DirectorRepository directorRepository) {
        this.filmStorage = filmStorage;
        this.genreRepository = genreRepository;
        this.likesRepository = likesRepository;
        this.userStorage = userStorage;
        this.filmRepository = filmRepository;
        this.directorRepository = directorRepository;
    }

    public Film createFilm(Film film) {
        if (!filmRepository.ratingExists(film.getMpa().getId())) {
            throw new NotFoundException("Rating with ID " + film.getMpa().getId() + " not found");
        }
        Film createdFilm = filmStorage.createFilm(film);
        if (!createdFilm.getGenres().isEmpty()) {
            for (Genre genre : createdFilm.getGenres()) {
                if (!genreRepository.genreExists(genre.getId())) {
                    throw new NotFoundException("Genre with ID " + genre.getId() + " not found");
                }
            }
            genreRepository.addGenres(createdFilm.getId(), createdFilm.getGenres()
                    .stream()
                    .map(Genre::getId)
                    .toList());
        }
        if (!createdFilm.getDirectors().isEmpty()) {
            for (Director director : createdFilm.getDirectors()) {
                if (!directorRepository.directorExists(director.getId())) {
                    throw new NotFoundException("Director with ID " + director.getId() + " not found");
                }
            }
            directorRepository.addDirector(createdFilm.getId(), createdFilm.getDirectors()
                    .stream()
                    .map(Director::getId)
                    .toList());
        }
        return createdFilm;
    }

    public Film updateFilm(Film filmUpdated) {
        Film existingFilm = filmStorage.getFilm(filmUpdated.getId());
        if (existingFilm == null) {
            throw new NotFoundException("Фильм с ID " + filmUpdated.getId() + " не найден");
        }
        if (!filmRepository.ratingExists(filmUpdated.getMpa().getId())) {
            throw new NotFoundException("Rating with ID " + filmUpdated.getMpa().getId() + " not found");
        }

        // 🔹 Обновляем сам фильм
        Film updatedFilm = filmStorage.updateFilm(filmUpdated);

        // 🔹 Удаляем все старые жанры и записываем новые (даже если список пуст)
        genreRepository.delGenres(updatedFilm.getId());
        if (filmUpdated.getGenres() != null && !filmUpdated.getGenres().isEmpty()) {
            genreRepository.addGenres(updatedFilm.getId(), filmUpdated.getGenres()
                    .stream()
                    .map(Genre::getId)
                    .toList());
        }

        // 🔹 Удаляем всех старых режиссёров и записываем новых (даже если список пуст)
        directorRepository.delDirector(updatedFilm.getId());
        if (filmUpdated.getDirectors() != null && !filmUpdated.getDirectors().isEmpty()) {
            directorRepository.addDirector(updatedFilm.getId(), filmUpdated.getDirectors()
                    .stream()
                    .map(Director::getId)
                    .toList());
        }

        return getFilm(updatedFilm.getId()); // ✅ Возвращаем фильм с актуальными данными
    }


    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilm(Integer id) {
        Film film = filmStorage.getFilm(id);
        film.setGenres(new LinkedHashSet<>(genreRepository.getAllGenresByFilmId(film.getId())));
        film.setDirectors(new LinkedHashSet<>(directorRepository.getAllDirectorsByFilmId(film.getId())));
        log.info("Обработан запрос на получение фильма с ID {}", id);
        return film;
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
    private Film setGenres(Film film) {
        film.setGenres(genreRepository.getAllGenresByFilmId(film.getId()));
        return film;
    }
    private Film setDirectors(Film film) {
        film.setDirectors(directorRepository.getAllDirectorsByFilmId(film.getId()));
        return film;
    }

    public Collection<Film> getFilmsByDirector(int directorId, String sortBy) {
        checkDirector(directorId);  // Проверка существования режиссёра

        // Получаем фильмы, отсортированные по году или лайкам
        Collection<Film> films;
        if ("likes".equalsIgnoreCase(sortBy)) {
            films = filmStorage.getByDirectorId(directorId, "likes");
        } else {
            films = filmStorage.getByDirectorId(directorId, "year");  // Сортировка по году
        }

        // Загружаем все жанры и режиссёров для этих фильмов
        Map<Integer, List<Genre>> genresByFilmId = genreRepository.getAllByFilms();
        Map<Integer, List<Director>> directorsByFilmId = directorRepository.findAllByFilms();

        // Добавляем жанры и режиссёров в каждый фильм
        films.forEach(film -> {
            film.setGenres(genresByFilmId.getOrDefault(film.getId(), List.of()));  // Устанавливаем жанры
            film.setDirectors(directorsByFilmId.getOrDefault(film.getId(), List.of()));  // Устанавливаем режиссёров
            System.out.println(film.getId());

        });

        return films;
    }
    private void checkDirector(int directorId) {
        if (directorRepository.getAllDirectorsByFilmId(directorId).isEmpty()) {
            throw new NotFoundException("Режисер не найден");
        }
    }
}
