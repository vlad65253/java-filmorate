package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.EventRowMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.EventStorage;

import java.util.List;

@Repository
public class EventRepository extends BaseRepository<Event> implements EventStorage {

    private static final String SQL_EVENT =
            "SELECT * FROM EVENTS WHERE USER_ID = ?";

    public EventRepository(JdbcTemplate jdbc, EventRowMapper eventRowMapper) {
        super(jdbc, eventRowMapper);
    }

    @Override
    public List<Event> getFeedUserById(int id) {
        return findMany(SQL_EVENT, id);
    }
}


