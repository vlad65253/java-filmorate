package ru.yandex.practicum.filmorate.service.enums;

public enum SortedBy {
    YEAR, LIKES;

    public static SortedBy from(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "year" -> YEAR;
            case "likes" -> LIKES;
            default -> null;
        };
    }
}