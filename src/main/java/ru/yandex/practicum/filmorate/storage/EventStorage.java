package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.dal.status.EventOperation;
import ru.yandex.practicum.filmorate.dal.status.EventType;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface EventStorage {

    List<Event> getFeedUserById(int id);

    void addEvent(int userId, EventType eventType, EventOperation operation, int entityId);

    void deleteEventsByUserId(int userId);
}
