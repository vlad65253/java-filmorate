package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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

    public void updateDirectors(Integer filmId, List<Integer> directorId) {
        directorRepository.addDirector(filmId, directorId);
    }

    public void deleteDirector(Integer filmId) {
        directorRepository.delDirector(filmId);
    }

    public Director createDirector(Director director) {
        try {
            return directorRepository.createDirector(director);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("Режиссер с таким именем уже существует: " + director.getName());
        }
    }

    public Director updateDirector(Director director) {
        getDirectorById(director.getId());
        try {
            return directorRepository.updateDirector(director);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("Режиссер с таким именем уже существует: " + director.getName());
        }
    }
}