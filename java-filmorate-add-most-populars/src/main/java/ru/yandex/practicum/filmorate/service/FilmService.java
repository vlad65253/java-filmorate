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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreRepository genreRepository;
    private final LikesRepository likesRepository;
    private final DirectorRepository directorRepository;
    private final FilmRepository filmRepository;

    public FilmService(@Autowired @Qualifier("filmRepository") FilmStorage filmStorage,
                       @Autowired @Qualifier("userRepository") UserStorage userStorage,
                       @Autowired GenreRepository genreRepository,
                       @Autowired LikesRepository likesRepository,
                       @Autowired DirectorRepository directorRepository,
                       @Autowired FilmRepository filmRepository) {
        this.filmStorage = filmStorage;
        this.genreRepository = genreRepository;
        this.likesRepository = likesRepository;
        this.userStorage = userStorage;
        this.directorRepository = directorRepository;
        this.filmRepository = filmRepository;
    }

    public Film createFilm(Film film) {
        if (!filmStorage.ratingExists(film.getMpa().getId())) {
            throw new NotFoundException("Рейтинг МПА с ID " + film.getMpa().getId() + " не найден");
        }
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                if (!filmStorage.genreTry(genre.getId())) {
                    throw new NotFoundException("Жанр с ID " + genre.getId() + " не найден");
                }
            }
        }
        Film createdFilm = filmStorage.createFilm(film);
        // Сохранение жанров
        Set<Genre> genres = film.getGenres();
        if (genres != null && !genres.isEmpty()) {
            genreRepository.addGenres(createdFilm.getId(), genres.stream()
                    .map(Genre::getId)
                    .toList());
        }
        // Сохранение фильмов
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            directorRepository.addDirector(createdFilm.getId(), film.getDirectors()
                    .stream()
                    .map(Director::getId)
                    .toList());
        }
        return createdFilm;
    }

    public Film updateFilm(Film filmUpdated) {
        if (filmStorage.getFilm(filmUpdated.getId()) == null) {
            throw new NotFoundException("Не передан идентификатор фильма");
        }
        if (!filmStorage.ratingExists(filmUpdated.getMpa().getId())) {
            throw new NotFoundException("Рейтинг МПА с ID " + filmUpdated.getMpa().getId() + " не найден");
        }
        if (filmUpdated.getGenres() != null && !filmUpdated.getGenres().isEmpty()) {
            for (Genre genre : filmUpdated.getGenres()) {
                if (!filmStorage.genreTry(genre.getId())) {
                    throw new NotFoundException("Жанр с ID " + genre.getId() + " не найден");
                }
            }
        }
        Film updatedFilm = filmStorage.updateFilm(filmUpdated);

        // Обновление жанров
        if (updatedFilm.getGenres() != null && !updatedFilm.getGenres().isEmpty()) {
            genreRepository.delGenres(updatedFilm.getId());
            genreRepository.addGenres(updatedFilm.getId(), updatedFilm.getGenres()
                    .stream()
                    .map(Genre::getId)
                    .toList());
        }

        // Обновление фильмов
        if (filmUpdated.getDirectors() != null && !filmUpdated.getDirectors().isEmpty()) {
            // Удаляем существующие связи с режиссёрами для этого фильма
            directorRepository.delDirector(updatedFilm.getId());
            // Добавляем новые связи
            directorRepository.addDirector(updatedFilm.getId(),
                    filmUpdated.getDirectors()
                            .stream()
                            .map(Director::getId)
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
        log.info("Пользователь {} оценил фильм {}", userId, filmId);
        return film;
    }

    public void delLikeFilm(Integer id, Integer userId) {
        userStorage.getUserById(userId); // также проверка на существующего пользователя

        likesRepository.deleteLike(id, userId);
        filmStorage.getFilm(id).getLikedList().remove(userId);
        log.info("Пользователь {} убрал оценку с фильма {}", userId, id);
    }

    public Collection<Film> getTopFilms(Integer count) {
        return filmStorage.getTopFilms(count);
    }

    public Collection<Film> getFilmsByDirector(int directorId, String sortBy) {
        if (directorRepository.getDirectorById(directorId) == null) {
            throw new NotFoundException("Режиссер с ID " + directorId + " не найден");
        }
        // Получаем фильмы, отсортированные по году или лайкам
        Collection<Film> films;
        if ("likes".equalsIgnoreCase(sortBy)) {
            films = filmStorage.getByDirectorId(directorId, "likes"); // По лайкам
        } else {
            films = filmStorage.getByDirectorId(directorId, "year");  // По году
        }

        // Загружаем все жанры и режиссёров для этих фильмов
        Map<Integer, Set<Genre>> genresByFilmId = genreRepository.getAllByFilms();
        Map<Integer, List<Director>> directorsByFilmId = directorRepository.findAllByFilms();

        // Добавляем жанры и режиссёров в каждый фильм
        films.forEach(film -> {
            film.setGenres(genresByFilmId.getOrDefault(film.getId(), Set.of()));  // Устанавливаем жанры
            film.setDirectors(directorsByFilmId.getOrDefault(film.getId(), List.of()));  // Устанавливаем режиссёров
            System.out.println(film.getId());

        });

        return films;
    }

    public Collection<Film> getCommonFilms(int userId, int friendId) {
        Collection<Film> commonFilms = filmStorage.getCommonFilms(userId, friendId);
        if (!commonFilms.isEmpty()) {
            // Загружаем все жанры и режиссёров для этих фильмов
            Map<Integer, Set<Genre>> genresByFilmId = genreRepository.getAllByFilms();
            Map<Integer, List<Director>> directorsByFilmId = directorRepository.findAllByFilms();

            // Добавляем жанры и режиссёров в каждый фильм
            commonFilms.forEach(film -> {
                film.setGenres(genresByFilmId.getOrDefault(film.getId(), Set.of()));  // Устанавливаем жанры
                film.setDirectors(directorsByFilmId.getOrDefault(film.getId(), List.of()));  // Устанавливаем режиссёров
                System.out.println(film.getId());

            });
        }
        return commonFilms;
    }

    //доделать
    public Collection<Film> getSearchFilms(String query, String by) {
        Collection<Film> searchingFilms = filmStorage.getSearchFilms(query, by);
        if (!searchingFilms.isEmpty()) {
            Map<Integer, Set<Genre>> genresByFilmId = genreRepository.getAllByFilms();
            Map<Integer, List<Director>> directorsByFilmId = directorRepository.findAllByFilms();

            searchingFilms.forEach(film -> {
                film.setGenres(genresByFilmId.getOrDefault(film.getId(), Set.of()));  // Устанавливаем жанры
                film.setDirectors(directorsByFilmId.getOrDefault(film.getId(), List.of()));  // Устанавливаем режиссёров
                System.out.println(film.getId());

            });
        }
        return searchingFilms;
    }

    public Collection<Film> getTopFilmsByGenreAndYear(int limit, Integer genreId, Integer year) {
        return filmRepository.getTopFilmsByGenreAndYear(limit, genreId, year);

    }
}

