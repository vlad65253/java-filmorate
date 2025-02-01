package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dal.LikesRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreRepository genreRepository;
    private final LikesRepository likesRepository;
    private final DirectorRepository directorRepository;

    public FilmService(@Autowired @Qualifier("filmRepository") FilmStorage filmStorage,
                       @Autowired @Qualifier("userRepository") UserStorage userStorage,
                       @Autowired GenreRepository genreRepository,
                       @Autowired LikesRepository likesRepository,
                       @Autowired DirectorRepository directorRepository) {
        this.filmStorage = filmStorage;
        this.genreRepository = genreRepository;
        this.likesRepository = likesRepository;
        this.userStorage = userStorage;
        this.directorRepository = directorRepository;
    }


    public Film createFilm(Film film) {
        if (!filmStorage.ratingExists(film.getMpa().getId())) {
            throw new NotFoundException("Rating with ID " + film.getMpa().getId() + " not found");
        }
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                if (!filmStorage.genreTry(genre.getId())) {
                    throw new NotFoundException("Жанр с ID " + genre.getId() + " не найден");
                }
            }
        }
        Film createdFilm = filmStorage.createFilm(film);
        Set<Genre> genres = film.getGenres();
        if (genres != null && !genres.isEmpty()) {
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
        if (!filmStorage.ratingExists(filmUpdated.getMpa().getId())) {
            throw new NotFoundException("Rating with ID " + filmUpdated.getMpa().getId() + " not found");
        }
        if (filmUpdated.getGenres() != null && !filmUpdated.getGenres().isEmpty()) {
            for (Genre genre : filmUpdated.getGenres()) {
                if (!filmStorage.genreTry(genre.getId())) {
                    throw new NotFoundException("Жанр с ID " + genre.getId() + " не найден");
                }
            }
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
    public Collection<Film> getFilmsByDirector(int directorId, String sortBy) {
        if(directorRepository.getDirectorById(directorId) == null){
            throw new NotFoundException("Режиссер с ID " + directorId + " не найден");
        }
        // Получаем фильмы, отсортированные по году или лайкам
        Collection<Film> films;
        if ("likes".equalsIgnoreCase(sortBy)) {
            films = filmStorage.getByDirectorId(directorId, "likes");
        } else {
            films = filmStorage.getByDirectorId(directorId, "year");  // Сортировка по году
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
    private void checkDirector(int directorId) {
        if (directorRepository.getAllDirectorsByFilmId(directorId).isEmpty()) {
            throw new NotFoundException("Режисер не найден");
        }
    }
    private void fillGenresAndDirectorsForFilms(Collection<Film> films, Map<Integer, LinkedHashSet<Genre>> genres,
                                                Map<Integer, LinkedHashSet<Director>> directors) {
        for (Film film : films) {
            if (genres.containsKey(film.getId())) {
                film.setGenres(new LinkedHashSet<>(genres.get(film.getId())));
            }
            if (directors.containsKey(film.getId())) {
                film.setDirectors(new LinkedHashSet<>(directors.get(film.getId())));
            }
        }
    }

}
