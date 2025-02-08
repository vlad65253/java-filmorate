package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorRepository directorRepository;

    public Collection<Director> getAllDirectors() {
        Collection<Director> directors = directorRepository.getAllDirectors();
        return (directors != null) ? directors : new ArrayList<>();
    }

    public Director getDirectorById(Integer id) {
        return directorRepository.getDirectorById(id);
    }

    /**
     * Добавляет для указанного фильма список режиссёров.
     */
    public void updateDirectors(Integer filmId, List<Integer> directorIds) {
        directorRepository.addDirector(filmId, directorIds);
    }

    /**
     * Удаляет режиссёра из таблицы DIRECTORS по ID.
     */
    public void deleteDirector(Integer directorId) {
        directorRepository.delDirectorTable(directorId);
    }

    public Director createDirector(Director director) {
        if (director.getName() == null || director.getName().trim().isEmpty()) {
            throw new ValidationException("Имя директора пустое");
        }
        return directorRepository.createDirector(director);
    }

    public Director updateDirector(Director director) {
        // Проверяем существование режиссёра
        getDirectorById(director.getId());
        if (director.getName() == null || director.getName().trim().isEmpty()) {
            throw new ValidationException("Имя директора пустое");
        }
        return directorRepository.updateDirector(director);
    }
}