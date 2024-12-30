package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.service.RatingService;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class RatingController {
    RatingService ratingService;
    @GetMapping
    public Collection<Rating> getAllRatings(){
        return ratingService.getAllRating();
    }
    @GetMapping("/{id}")
    public Rating getRatingById(@PathVariable int id){
        return ratingService.getRatingById(id);
    }
}
