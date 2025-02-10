package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.status.EventOperation;
import ru.yandex.practicum.filmorate.dal.status.EventType;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.*;
import java.util.stream.Collectors;

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
    private final EventStorage eventStorage;

    public Film createFilm(@Valid Film film) {
        // Проверяем существование рейтинга МПА
        if (!filmStorage.ratingExists(film.getMpa().getId())) {
            throw new NotFoundException("Рейтинг МПА с ID " + film.getMpa().getId() + " не найден");
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            film.getGenres().forEach(g -> genreStorage.getGenreById(g.getId()));
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            film.getDirectors().forEach(g -> directorStorage.getDirectorById(g.getId()));
        }
        // Создаем фильм
        filmStorage.createFilm(film);
        film.setMpa(ratingStorage.getRatingById(film.getMpa().getId()));

        if (film.getGenres() != null) {
            genreStorage.createGenresForFilmById(film.getId(), film.getGenres().stream().toList());
            film.setGenres(genreStorage.getGenresFilmById(film.getId()));
        }

        if (film.getDirectors() != null) {
            directorStorage.createDirectorsForFilmById(film.getId(), film.getDirectors().stream().toList());
            film.setDirectors(directorStorage.getDirectorsFilmById(film.getId()));
        }
        return filmStorage.getFilmById(film.getId());
    }

    public Film updateFilm(@Valid Film film) {
        // Проверка существования фильма (метод getFilmById выбросит исключение, если фильм не найден)
        filmStorage.getFilmById(film.getId());

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
        film.setMpa(ratingStorage.getRatingById(film.getMpa().getId()));

        // Возвращаем обновлённый фильм
        return filmStorage.getFilmById(film.getId());
    }

    public Set<Film> getFilms() {
        return filmStorage.getFilms().stream()
                .sorted(Comparator.comparing(Film::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Film getFilmById(int id) {
        Film film = filmStorage.getFilmById(id);
        log.debug("Получен фильм: {}", film);
        return film;
    }

    public void deleteFilm(int id) {
        filmStorage.deleteFilm(id);
        log.info("Удалён фильм с id: {}", id);
    }

    public Set<Film> getTopFilms(int count, Integer genreId, Integer year) {
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

    public List<Film> getCommonFilms(int userId, int friendId) {
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
                .map(filmStorage::getFilmById)
                .toList();

        // Сортируем фильмы по количеству лайков (популярности) по убыванию
        List<Film> sortedFilms = commonFilms.stream()
                .sorted((f1, f2) -> Integer.compare(
                        likesStorage.getLikeCountForFilm(f2.getId()),
                        likesStorage.getLikeCountForFilm(f1.getId())
                ))
                .collect(Collectors.toList());
        // Мы поспорили, нужен ли здесь LinkedList для сортировки, или она сохранится при использовании List
        // Уважаемый ревьюер, прошу решить наш спор
        log.debug("Получены общие фильмы для пользователей {} и {}: {}", userId, friendId, sortedFilms);
        return sortedFilms;
    }

    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        // Проверка существования режиссёра
        directorStorage.getDirectorById(directorId);

        // Получаем фильмы режиссера через простой SQL-запрос
        List<Film> films = filmStorage.getFilmsByDirector(directorId);
        List<Film> filmList = new ArrayList<>(films);

        // Выполняем сортировку в зависимости от параметра sortBy
        if ("year".equalsIgnoreCase(sortBy)) {
            // Сортировка по году выпуска (по возрастанию)
            filmList.sort(Comparator.comparing(Film::getReleaseDate));
        } else if ("likes".equalsIgnoreCase(sortBy)) {
            // Сортировка по количеству лайков (по убыванию)
            filmList.sort((f1, f2) -> Integer.compare(
                    likesStorage.getLikeCountForFilm(f2.getId()),
                    likesStorage.getLikeCountForFilm(f1.getId())
            ));
        } else {
            throw new ValidationException("Некорректный параметр сортировки: " + sortBy);
        }

        log.debug("Получены фильмы режиссера {} с сортировкой '{}': {}", directorId, sortBy, filmList);
        return filmList;
    }

    public List<Film> searchFilms(String query, String by) {
        String searchParam = "%" + query.toLowerCase() + "%";
        Set<Film> result = new HashSet<>();
        String[] criteria = by.split(",");
        for (String criterion : criteria) {
            String trimmed = criterion.trim();
            if ("title".equalsIgnoreCase(trimmed)) {
                result.addAll(filmStorage.getFilmsByTitle(searchParam));
            } else if ("director".equalsIgnoreCase(trimmed)) {
                result.addAll(filmStorage.getFilmsByDirectorName(searchParam));
            } else {
                throw new ValidationException("Некорректное значение параметра 'by': " + trimmed);
            }
        }
        // Сортировка по количеству лайков (популярности)
        List<Film> sortedFilms = result.stream()
                .sorted((f1, f2) -> Integer.compare(
                        likesStorage.getLikeCountForFilm(f2.getId()),
                        likesStorage.getLikeCountForFilm(f1.getId())
                ))
                .collect(Collectors.toList());
        log.debug("Результаты поиска для query='{}', by='{}': {}", query, by, sortedFilms);
        return sortedFilms;
    }

    public Film likeFilm(int filmId, int userId) {
        filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);
        likesStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        eventStorage.addEvent(userId, EventType.LIKE, EventOperation.ADD, filmId);
        return filmStorage.getFilmById(filmId);
    }

    public void delLikeFilm(int filmId, int userId) {
        filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);
        likesStorage.deleteLike(filmId, userId);
        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
        eventStorage.addEvent(userId, EventType.LIKE, EventOperation.REMOVE, filmId);
    }
}
