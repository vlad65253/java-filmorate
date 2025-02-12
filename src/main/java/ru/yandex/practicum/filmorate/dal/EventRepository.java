package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.status.EventOperation;
import ru.yandex.practicum.filmorate.dal.status.EventType;
import ru.yandex.practicum.filmorate.mapper.EventRowMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.EventStorage;

import java.util.List;

@Repository
public class EventRepository extends BaseRepository<Event> implements EventStorage {

    public EventRepository(JdbcTemplate jdbc, EventRowMapper eventRowMapper) {
        super(jdbc, eventRowMapper);
    }

    @Override
    public List<Event> getFeedUserById(int id) {
        return findMany("SELECT * FROM EVENTS WHERE USER_ID = ?", id);
    }

    @Override
    public void addEvent(int userId, EventType eventType, EventOperation operation, int entityId) {
        update("INSERT INTO EVENTS(USER_ID, EVENT_TYPE, OPERATION, ENTITY_ID) VALUES (?, ?, ?, ?)",
                userId, eventType.toString(), operation.toString(), entityId);
    }

    @Override
    public void deleteEventsByUserId(int userId) {
        delete("DELETE FROM EVENTS WHERE USER_ID = ?", userId);
    }
}