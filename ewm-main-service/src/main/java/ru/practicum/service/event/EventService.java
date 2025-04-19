package ru.practicum.service.event;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.dto.event.*;
import ru.practicum.dto.participationRequest.EventRequestStatusUpdateRequest;
import ru.practicum.dto.participationRequest.EventRequestStatusUpdateResult;
import ru.practicum.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.model.EventSearchParams;
import ru.practicum.model.EventSort;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getUserEvents(Long userId, int from, int size);

    EventFullDto getUserEventById(Long userId, Long eventId);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest);

    // --- ADMIN API ---

    List<EventFullDto> searchEvents(List<Long> users, List<String> states,
                                    List<Long> categories, LocalDateTime rangeStart,
                                    LocalDateTime rangeEnd, int from, int size);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest);

    // --- PUBLIC API ---

    List<EventShortDto> getPublicEvents(EventSearchParams params, String sort, int from, int size, HttpServletRequest request);

    EventFullDto getPublicEventById(Long id, HttpServletRequest request);

    // --- PARTICIPATION REQUESTS ---

    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatuses(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest);

    List<EventShortDto> getPublicEventsWithFilters(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            EventSort sort,
            Pageable pageable,
            HttpServletRequest request
    );


}
