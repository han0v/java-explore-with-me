package ru.practicum.repository.event;

import org.springframework.data.domain.Pageable;
import ru.practicum.model.Event;
import ru.practicum.model.EventSort;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepositoryCustom {
    List<Event> findPublicEventsWithFilters(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            EventSort sort,
            Pageable pageable
    );
}
