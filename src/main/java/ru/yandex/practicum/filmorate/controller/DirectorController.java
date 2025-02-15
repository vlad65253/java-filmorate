package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Set;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public Set<Director> getAllDirectors() {
        return directorService.getDirectors();
    }

    @GetMapping("/{id}")
    public Director getGenreById(@PathVariable int id) {
        return directorService.getDirectorById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Director createDirector(@RequestBody Director director) {
        return directorService.createDirector(director);
    }

    @PutMapping
    public Director updateDirector(@RequestBody Director director) {
        return directorService.updateDirector(director);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delDirector(@PathVariable("id") int directorId) {
        directorService.deleteDirectorById(directorId);
    }
}