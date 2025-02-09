package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Validated
@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorRepository directorStorage;

    public Set<Director> getDirectors() {
        return directorStorage.getDirectors().stream()
                .sorted(Comparator.comparing(Director::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Director getDirectorById(int id) {
        return directorStorage.getDirectorById(id)
                .orElseThrow(() -> new NotFoundException("Режиссер не найден."));
    }

    public Director createDirector(Director director) {
        if (director.getName().isBlank()) {
            throw new ValidationException("Поле name не может быть пустым.");
        }
        return directorStorage.createDirector(director);
    }

    public Director updateDirector(Director director) {
        directorStorage.getDirectorById(director.getId())
                .orElseThrow(() -> new NotFoundException("Режиссер не найден."));
        if (director.getName().isBlank()) {
            throw new ValidationException("Поле name не может быть пустым.");
        }
        return directorStorage.updateDirector(director);
    }

    public void deleteDirectorById(int id) {
        directorStorage.getDirectorById(id)
                .orElseThrow(() -> new ValidationException("Режиссер не найден."));
        directorStorage.deleteDirectorById(id);
    }

    public void createDirectorsForFilmById(int filmId, List<Director> directorsId) {
        directorStorage.createDirectorsForFilmById(filmId, directorsId);
    }

    public Set<Director> getDirectorsFilmById(int filmId) {
        return directorStorage.getDirectorsFilmById(filmId);
    }
}

