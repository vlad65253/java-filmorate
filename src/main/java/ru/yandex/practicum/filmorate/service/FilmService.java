package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.status.EventOperation;
import ru.yandex.practicum.filmorate.dal.status.EventType;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.enums.SortedBy;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.LikesStorage;
import ru.yandex.practicum.filmorate.storage.RatingStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

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
        if (!ratingStorage.ratingExists(film.getMpa().getId())) {
            throw new NotFoundException("Рейтинг МПА с ID " + film.getMpa().getId() + " не найден");
        }
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            film.getGenres().forEach(g -> genreStorage.getGenreById(g.getId()));
        }
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            film.getDirectors().forEach(d -> directorStorage.getDirectorById(d.getId()));
        }
        Film createdFilm = filmStorage.createFilm(film);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreStorage.createGenresForFilmById(createdFilm.getId(), film.getGenres().stream().toList());
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            directorStorage.createDirectorsForFilmById(createdFilm.getId(), film.getDirectors().stream().toList());
        }
        // Получаем фильм с JOIN для рейтинга
        createdFilm = filmStorage.getFilmById(createdFilm.getId());
        // Обогащаем фильм жанрами и режиссёрами через единоразовую выборку по ID
        createdFilm.setGenres(genreStorage.getGenresByFilmId(createdFilm.getId())
                .stream().sorted(Comparator.comparing(Genre::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        createdFilm.setDirectors(directorStorage.getDirectorsFilmById(createdFilm.getId()));
        return createdFilm;
    }

    public Film updateFilm(Film film) {
        filmStorage.getFilmById(film.getId());

        if (!ratingStorage.ratingExists(film.getMpa().getId())) {
            throw new NotFoundException("Рейтинг МПА с ID " + film.getMpa().getId() + " не найден");
        }
        // Обновляем жанры: сначала удаляем, затем создаем, затем обогащаем
        genreStorage.deleteGenreForFilmById(film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreStorage.createGenresForFilmById(film.getId(), film.getGenres().stream().toList());
        } else {
            film.setGenres(Collections.emptySet());
        }
        // Обновляем режиссёров аналогичным образом
        directorStorage.deleteDirectorsFilmById(film.getId());
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            directorStorage.createDirectorsForFilmById(film.getId(), film.getDirectors().stream().toList());
        } else {
            film.setDirectors(Collections.emptySet());
        }
        // Обновляем основные данные фильма
        filmStorage.updateFilm(film);
        // Получаем обновленный фильм с JOIN для рейтинга
        Film updatedFilm = filmStorage.getFilmById(film.getId());
        // Обогащаем фильм жанрами и режиссёрами
        updatedFilm.setGenres(genreStorage.getGenresByFilmId(updatedFilm.getId())
                .stream().sorted(Comparator.comparing(Genre::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        updatedFilm.setDirectors(directorStorage.getDirectorsFilmById(updatedFilm.getId()));
        return updatedFilm;
    }

    public Set<Film> getFilms() {
        List<Film> films = filmStorage.getFilms();
        films = getAttributesForFilm(films);
        return films.stream()
                .sorted(Comparator.comparing(Film::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Film getFilmById(int id) {
        Film film = filmStorage.getFilmById(id);
        film.setGenres(genreStorage.getGenresByFilmId(film.getId())
                .stream().sorted(Comparator.comparing(Genre::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        film.setDirectors(directorStorage.getDirectorsFilmById(film.getId()));
        log.debug("Получен фильм: {}", film);
        return film;
    }

    public void deleteFilm(int id) {
        filmStorage.deleteFilm(id);
        log.info("Удалён фильм с id: {}", id);
    }

    public Set<Film> getTopFilms(int count, Integer genreId, Integer year) {
        List<Film> films = filmStorage.getFilms();
        films = getAttributesForFilm(films);
        if (genreId != null) {
            films = films.stream()
                    .filter(film -> film.getGenres().stream().anyMatch(genre -> genre.getId().equals(genreId)))
                    .collect(Collectors.toList());
        }
        if (year != null) {
            films = films.stream()
                    .filter(film -> film.getReleaseDate().getYear() == year)
                    .collect(Collectors.toList());
        }
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
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);
        Set<Integer> userLikedFilms = likesStorage.getLikedFilmsByUser(userId);
        Set<Integer> friendLikedFilms = likesStorage.getLikedFilmsByUser(friendId);
        userLikedFilms.retainAll(friendLikedFilms);
        if (userLikedFilms.isEmpty()) {
            log.info("Нет общих фильмов для пользователей {} и {}", userId, friendId);
            return Collections.emptyList();
        }
        List<Film> commonFilms = userLikedFilms.stream()
                .map(filmStorage::getFilmById)
                .toList();
        List<Film> sortedFilms = commonFilms.stream()
                .sorted((f1, f2) -> Integer.compare(
                        likesStorage.getLikeCountForFilm(f2.getId()),
                        likesStorage.getLikeCountForFilm(f1.getId())
                ))
                .collect(Collectors.toList());
        log.debug("Получены общие фильмы для пользователей {} и {}: {}", userId, friendId, sortedFilms);
        return getAttributesForFilm(sortedFilms);
    }

    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        directorStorage.getDirectorById(directorId);
        List<Film> films = filmStorage.getFilmsByDirector(directorId);
        List<Film> filmList = new ArrayList<>(films);
        if (SortedBy.from(sortBy) == SortedBy.YEAR) {
            filmList.sort(Comparator.comparing(Film::getReleaseDate));
        } else if (SortedBy.from(sortBy) == SortedBy.LIKES) {
            filmList.sort((f1, f2) -> Integer.compare(
                    likesStorage.getLikeCountForFilm(f2.getId()),
                    likesStorage.getLikeCountForFilm(f1.getId())
            ));
        } else {
            throw new ValidationException("Некорректный параметр сортировки: " + sortBy);
        }
        log.debug("Получены фильмы режиссёра {} с сортировкой '{}': {}", directorId, sortBy, filmList);
        return getAttributesForFilm(filmList);
    }

    public List<Film> searchFilms(String query, String by) {
        if (query == null || query.isBlank()) {
            throw new ValidationException("Строка поиска не может быть пустой");
        }
        String searchParam = "%" + query.toLowerCase() + "%";
        Set<Film> result = new HashSet<>();
        String[] criteria = by.split(",");
        for (String criterion : criteria) {
            if ("title".equalsIgnoreCase(criterion.trim())) {
                result.addAll(filmStorage.getFilmsByTitle(searchParam));
            } else if ("director".equalsIgnoreCase(criterion.trim())) {
                result.addAll(filmStorage.getFilmsByDirectorName(searchParam));
            } else {
                throw new ValidationException("Некорректное значение параметра 'by': " + criterion);
            }
        }
        List<Film> sortedFilms = result.stream()
                .sorted((f1, f2) -> Integer.compare(
                        likesStorage.getLikeCountForFilm(f2.getId()),
                        likesStorage.getLikeCountForFilm(f1.getId())
                ))
                .collect(Collectors.toList());
        log.debug("Результаты поиска для query='{}', by='{}': {}", query, by, sortedFilms);
        return getAttributesForFilm(sortedFilms);
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

    public List<Film> getRecommendations(int userId) {
        userStorage.getUserById(userId);
        List<Film> recommendedFilms = filmStorage.getRecommendations(userId);
        if (recommendedFilms.isEmpty()) {
            log.info("Нет рекомендованных фильмов для пользователя с ID {}", userId);
            return recommendedFilms;
        }
        log.info("Получены рекомендации для пользователя с ID {}: {}", userId, recommendedFilms);
        return getAttributesForFilm(recommendedFilms);
    }

    public List<Film> getAttributesForFilm(List<Film> films) {
        Set<Integer> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());
        Map<Integer, Set<Genre>> genresMap = genreStorage.getGenresForFilmIds(filmIds);
        Map<Integer, Set<Director>> directorsMap = directorStorage.getDirectorsForFilmIds(filmIds);
        films.forEach(film -> {
            film.setGenres(genresMap.getOrDefault(film.getId(), Collections.emptySet())
                    .stream().sorted(Comparator.comparing(Genre::getId))
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
            film.setDirectors(directorsMap.getOrDefault(film.getId(), Collections.emptySet()));
        });
        return films;
    }
}