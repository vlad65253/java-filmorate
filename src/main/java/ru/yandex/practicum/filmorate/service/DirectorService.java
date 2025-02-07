package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
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

    public void deleteDirector(Integer directorId) {
        directorRepository.delDirectorTable(directorId);
    }

    public Director createDirector(Director director) {
        assert director.getName() != null;
        if(director.getName().trim().isEmpty()){
            throw new ValidationException("Имя директора пустое");
        }
        return directorRepository.createDirector(director);
    }

    public Director updateDirector(Director director) {
        getDirectorById(director.getId());
        assert director.getName() != null;
        if(director.getName().trim().isEmpty()){
            throw new ValidationException("Имя директора пустое");
        }
        return directorRepository.updateDirector(director);
    }
}