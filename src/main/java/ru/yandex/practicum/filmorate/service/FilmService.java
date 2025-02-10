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
        if(film.getGenres() != null && !film.getGenres().isEmpty()) {
            film.getGenres().forEach(g -> genreStorage.getGenreById(g.getId()));
        }
        if(film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            film.getDirectors().forEach(g -> directorStorage.getDirectorById(g.getId()));
        }
        // Создаем фильм
        filmStorage.createFilm(film);
        film.setMpa(ratingStorage.getRatingById(film.getMpa().getId()).get());

        if (film.getGenres() != null) {
            genreStorage.createGenresForFilmById(film.getId(), film.getGenres().stream().toList());
            film.setGenres(genreStorage.getGenresFilmById(film.getId()));
        }

        if (film.getDirectors() != null) {
            directorStorage.createDirectorsForFilmById(film.getId(), film.getDirectors().stream().toList());
            film.setDirectors(directorStorage.getDirectorsFilmById(film.getId()));
        }
        return filmStorage.getFilmById(film.getId()).get();
    }

    public Film updateFilm(Film film) {
        // Проверка существования фильма (метод getFilmById выбросит исключение, если фильм не найден)
        filmStorage.getFilmById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id " + film.getId() + " не найден"));

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

        // Если жанры заданы и список не пустой – обновляем их,
        // иначе (если список пуст или равен null) – просто удаляем все привязанные жанры.
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreStorage.deleteGenreForFilmById(film.getId());
            genreStorage.createGenresForFilmById(film.getId(), film.getGenres().stream().toList());
            film.setGenres(genreStorage.getGenresFilmById(film.getId()));
        } else {
            genreStorage.deleteGenreForFilmById(film.getId());
            film.setGenres(Collections.emptySet());
        }

        // Аналогичная логика для режиссёров (если требуется). Здесь можно оставить как есть, либо аналогично:
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            directorStorage.deleteDirectorsFilmById(film.getId());
            directorStorage.createDirectorsForFilmById(film.getId(), film.getDirectors().stream().toList());
            film.setDirectors(directorStorage.getDirectorsFilmById(film.getId()));
        } else {
            directorStorage.deleteDirectorsFilmById(film.getId());
            film.setDirectors(Collections.emptySet());
        }

        // Обновляем основные данные фильма
        filmStorage.updateFilm(film);
        film.setMpa(ratingStorage.getRatingById(film.getMpa().getId()).get());

        // Возвращаем обновлённый фильм
        return filmStorage.getFilmById(film.getId()).get();
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
        List<Film> films = filmStorage.getFilms();

        // Если задан параметр genreId – оставляем только те фильмы, у которых есть жанр с таким ID
        if (genreId != null) {
            films = films.stream()
                    .filter(film -> film.getGenres() != null &&
                            film.getGenres().stream().anyMatch(genre -> genre.getId().equals(genreId)))
                    .collect(Collectors.toList());
        }

        // Если задан параметр year – оставляем только фильмы, выпущенные в указанном году
        if (year != null) {
            films = films.stream()
                    .filter(film -> film.getReleaseDate() != null &&
                            film.getReleaseDate().getYear() == year)
                    .collect(Collectors.toList());
        }

        // Сортируем фильмы по количеству лайков по убыванию
        films.sort((f1, f2) -> Integer.compare(
                likesStorage.getLikeCountForFilm(f2.getId()),
                likesStorage.getLikeCountForFilm(f1.getId())
        ));

        Set<Film> topFilms = films.stream()
                .limit(count)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        log.debug("Получены топ-{} фильмов по лайкам с genreId={} и year={}: {}", count, genreId, year, topFilms);
        return topFilms;
    }

    public Collection<Film> getCommonFilms(int userId, int friendId) {
        // Проверка существования пользователей (метод getUserById выбросит исключение, если пользователь не найден)
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);

        // Получаем наборы ID фильмов, которые лайкнули оба пользователя
        Set<Integer> userLikedFilms = likesStorage.getLikedFilmsByUser(userId);
        Set<Integer> friendLikedFilms = likesStorage.getLikedFilmsByUser(friendId);

        // Вычисляем пересечение наборов
        userLikedFilms.retainAll(friendLikedFilms);

        if (userLikedFilms.isEmpty()) {
            log.info("Нет общих фильмов для пользователей {} и {}", userId, friendId);
            return Collections.emptyList();
        }

        // Получаем фильмы по ID из пересечения без использования findFilmsByIds:
        List<Film> commonFilms = userLikedFilms.stream()
                .map(filmId -> filmStorage.getFilmById(filmId).get())
                .collect(Collectors.toList());

        // Сортируем фильмы по количеству лайков (популярности) по убыванию
        List<Film> sortedFilms = commonFilms.stream()
                .sorted((f1, f2) -> Integer.compare(
                        likesStorage.getLikeCountForFilm(f2.getId()),
                        likesStorage.getLikeCountForFilm(f1.getId())
                ))
                .collect(Collectors.toList());

        log.debug("Получены общие фильмы для пользователей {} и {}: {}", userId, friendId, sortedFilms);
        return sortedFilms;
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
        Film film = filmStorage.getFilmById(filmId).get();
        userStorage.getUserById(userId);
        likesStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        return filmStorage.getFilmById(filmId).get();
    }

    public void delLikeFilm(Integer filmId, Integer userId) {
        filmStorage.getFilmById(filmId).get();
        userStorage.getUserById(userId);
        likesStorage.deleteLike(filmId, userId);
        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }
}
