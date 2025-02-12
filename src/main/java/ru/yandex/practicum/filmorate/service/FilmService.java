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
import ru.yandex.practicum.filmorate.service.enums.SortedBy;
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

    public Film createFilm(Film film) {
        // Проверяем существование рейтинга МПА
        if (ratingStorage.ratingExists(film.getMpa().getId())) {
            throw new NotFoundException("Рейтинг МПА с ID " + film.getMpa().getId() + " не найден");
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            film.getGenres().forEach(g -> genreStorage.getGenreById(g.getId()));
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            film.getDirectors().forEach(g -> directorStorage.getDirectorById(g.getId()));
        }
        // Создаем фильм
        Film createFilm = filmStorage.createFilm(film);
        film.setMpa(ratingStorage.getRatingById(film.getMpa().getId()));

        if (film.getGenres() != null) {
            genreStorage.createGenresForFilmById(film.getId(), film.getGenres().stream().toList());
            film.setGenres(genreStorage.getGenresFilmById(film.getId()));
        }

        if (film.getDirectors() != null) {
            directorStorage.createDirectorsForFilmById(film.getId(), film.getDirectors().stream().toList());
            film.setDirectors(directorStorage.getDirectorsFilmById(film.getId()));
        }
        return film;
    }

    public Film updateFilm(@Valid Film film) {
        // Проверка существования фильма (метод getFilmById выбросит исключение, если фильм не найден)
        filmStorage.getFilmById(film.getId());

        // Проверяем существование рейтинга МПА
        if (ratingStorage.ratingExists(film.getMpa().getId())) {
            throw new NotFoundException("Рейтинг МПА с ID " + film.getMpa().getId() + " не найден");
        }

        // Если жанры заданы и список не пустой – обновляем их,
        // иначе (если список пуст или равен null) – просто удаляем все привязанные жанры.
        genreStorage.deleteGenreForFilmById(film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreStorage.createGenresForFilmById(film.getId(), film.getGenres().stream().toList());
            film.setGenres(genreStorage.getGenresFilmById(film.getId()));
        } else {
            film.setGenres(Collections.emptySet());
        }

        // Аналогичная логика для режиссёров (если требуется). Здесь можно оставить как есть, либо аналогично:
        directorStorage.deleteDirectorsFilmById(film.getId());
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            directorStorage.createDirectorsForFilmById(film.getId(), film.getDirectors().stream().toList());
            film.setDirectors(directorStorage.getDirectorsFilmById(film.getId()));
        } else {
            film.setDirectors(Collections.emptySet());
        }

        // Обновляем основные данные фильма
        filmStorage.updateFilm(film);
        film.setMpa(ratingStorage.getRatingById(film.getMpa().getId()));

        // Возвращаем обновлённый фильм
        return film;
    }

    public Set<Film> getFilms() {
        return filmStorage.getFilms().stream()
                .sorted(Comparator.comparing(Film::getId))
                .peek(film -> {
                    film.setDirectors(directorStorage.getDirectorsFilmById(film.getId()));
                    film.setGenres(genreStorage.getGenresFilmById(film.getId()));
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));

    }

    public Film getFilmById(int id) {
        Film film = filmStorage.getFilmById(id);
        film.setDirectors(directorStorage.getDirectorsFilmById(film.getId()));
        film.setGenres(genreStorage.getGenresFilmById(film.getId()));
        log.debug("Получен фильм: {}", film);
        return film;
    }

    public void deleteFilm(int id) {
        filmStorage.deleteFilm(id);
        log.info("Удалён фильм с id: {}", id);
    }

    public Set<Film> getTopFilms(int count, Integer genreId, Integer year) {
        List<Film> films = filmStorage.getFilms().stream()
                .peek(film -> {
                    film.setDirectors(directorStorage.getDirectorsFilmById(film.getId()));
                    film.setGenres(genreStorage.getGenresFilmById(film.getId()));
                })
                .collect(Collectors.toList());
        ;

        // Если задан параметр genreId – оставляем только те фильмы, у которых есть жанр с таким ID
        if (genreId != null) {
            films = films.stream()
                    .filter(film -> film.getGenres() != null &&
                            film.getGenres().stream().anyMatch(genre -> genre.getId().equals(genreId)))
                    .peek(film -> {
                        film.setDirectors(directorStorage.getDirectorsFilmById(film.getId()));
                        film.setGenres(genreStorage.getGenresFilmById(film.getId()));
                    })
                    .collect(Collectors.toList());
        }

        // Если задан параметр year – оставляем только фильмы, выпущенные в указанном году
        if (year != null) {
            films = films.stream()
                    .filter(film -> film.getReleaseDate() != null &&
                            film.getReleaseDate().getYear() == year)
                    .peek(film -> {
                        film.setDirectors(directorStorage.getDirectorsFilmById(film.getId()));
                        film.setGenres(genreStorage.getGenresFilmById(film.getId()));
                    })
                    .collect(Collectors.toList());
        }

        // Сортируем фильмы по количеству лайков по убыванию
        films.sort((f1, f2) -> Integer.compare(
                likesStorage.getLikeCountForFilm(f2.getId()),
                likesStorage.getLikeCountForFilm(f1.getId())
        ));

        Set<Film> topFilms = films.stream()
                .limit(count)
                .peek(film -> {
                    film.setDirectors(directorStorage.getDirectorsFilmById(film.getId()));
                    film.setGenres(genreStorage.getGenresFilmById(film.getId()));
                })
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
                .peek(film -> {
                    film.setDirectors(directorStorage.getDirectorsFilmById(film.getId()));
                    film.setGenres(genreStorage.getGenresFilmById(film.getId()));
                })
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
        if (SortedBy.from(sortBy) == SortedBy.YEAR) {
            // Сортировка по году выпуска (по возрастанию)
            filmList.sort(Comparator.comparing(Film::getReleaseDate));
            filmList.stream()
                    .peek(film -> {
                        film.setDirectors(directorStorage.getDirectorsFilmById(film.getId()));
                        film.setGenres(genreStorage.getGenresFilmById(film.getId()));
                    })
                    .collect(Collectors.toList());
        } else {
            if (SortedBy.from(sortBy) == SortedBy.LIKES) {
                // Сортировка по количеству лайков (по убыванию)
                filmList.sort((f1, f2) -> Integer.compare(
                        likesStorage.getLikeCountForFilm(f2.getId()),
                        likesStorage.getLikeCountForFilm(f1.getId())
                ));
            } else {
                throw new ValidationException("Некорректный параметр сортировки: " + sortBy);
            }
            filmList.stream()
                    .peek(film -> {
                        film.setDirectors(directorStorage.getDirectorsFilmById(film.getId()));
                        film.setGenres(genreStorage.getGenresFilmById(film.getId()));
                    })
                    .collect(Collectors.toList());
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
                .peek(film -> {
                    film.setDirectors(directorStorage.getDirectorsFilmById(film.getId()));
                    film.setGenres(genreStorage.getGenresFilmById(film.getId()));
                })
                .collect(Collectors.toList());
        log.debug("Результаты поиска для query='{}', by='{}': {}", query, by, sortedFilms);
        return sortedFilms;
    }

    public Film likeFilm(int filmId, int userId) {
        Film film = filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);
        eventStorage.addEvent(userId, EventType.LIKE, EventOperation.ADD, filmId);

        Set<Integer> likedFilms = likesStorage.getLikedFilmsByUser(userId);
        if (likedFilms.contains(filmId)) {
            log.info("Пользователь {} уже поставил лайк фильму {}", userId, filmId);
            return film;
        }

        likesStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
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
