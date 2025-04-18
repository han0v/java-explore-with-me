package ru.practicum.service.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;
import ru.practicum.dto.event.*;
import ru.practicum.dto.participationRequest.EventRequestStatusUpdateRequest;
import ru.practicum.dto.participationRequest.EventRequestStatusUpdateResult;
import ru.practicum.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.*;
import ru.practicum.repository.category.CategoryRepository;
import ru.practicum.repository.event.EventRepository;
import ru.practicum.repository.request.RequestRepository;
import ru.practicum.repository.user.UserRepository;
import ru.practicum.stats_client.StatsClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;
    private final EventMapper eventMapper;
    private final RequestMapper requestMapper;


    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        Event event = eventMapper.toEvent(newEventDto);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setState(EventState.PENDING);

        Event savedEvent = eventRepository.save(event);
        return eventMapper.toEventFullDto(savedEvent, 0L, 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        return eventRepository.findAllByInitiatorId(userId, PageRequest.of(from / size, size)).stream()
                .map(event -> eventMapper.toEventShortDto(event, getConfirmedRequests(event.getId()), getViews(event.getId())))
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        return eventMapper.toEventFullDto(event, getConfirmedRequests(eventId), getViews(eventId));
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Cannot modify published event");
        }

        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction()) {
                case "SEND_TO_REVIEW":
                    event.setState(EventState.PENDING);
                    break;
                case "CANCEL_REVIEW":
                    event.setState(EventState.CANCELED);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid stateAction");
            }
        }

        updateEventFields(event, updateRequest);
        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toEventFullDto(updatedEvent, getConfirmedRequests(eventId), getViews(eventId));
    }


    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> searchEvents(
            List<Long> users,
            List<EventState> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            int from,
            int size) {

        Pageable pageable = PageRequest.of(
                from / size,
                size,
                Sort.by("eventDate").descending()
        );

        List<Event> events = eventRepository.findEventsByAdminParams(
                users, states, categories,
                rangeStart, rangeEnd,
                pageable
        );

        return events.stream()
                .map(event -> eventMapper.toEventFullDto(
                        event,
                        getConfirmedRequests(event.getId()),
                        getViews(event.getId())
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction()) {
                case "PUBLISH_EVENT":
                    if (event.getState() != EventState.PENDING) {
                        throw new ConflictException("Cannot publish event that is not pending");
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case "REJECT_EVENT":
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new ConflictException("Cannot reject already published event");
                    }
                    event.setState(EventState.CANCELED);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid stateAction");
            }
        }

        updateEventFields(event, updateRequest);
        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toEventFullDto(updatedEvent, getConfirmedRequests(eventId), getViews(eventId));
    }


    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getPublicEvents(EventSearchParams params, String sort, int from, int size, HttpServletRequest request) {
        saveEventViewStats(null, request);

        Pageable pageable = PageRequest.of(from / size, size);
        if ("EVENT_DATE".equalsIgnoreCase(sort)) {
            pageable = PageRequest.of(from / size, size, Sort.by("eventDate"));
        }

        List<Event> events = eventRepository.findPublicEvents(
                params.getText(),
                params.getCategories(),
                params.getPaid(),
                params.getRangeStart(),
                params.getRangeEnd(),
                params.getOnlyAvailable(),
                pageable
        );

        return events.stream()
                .map(event -> eventMapper.toEventShortDto(event, getConfirmedRequests(event.getId()), getViews(event.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getPublicEventById(Long id, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndState(id, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found or not published"));

        saveEventViewStats(id, request);
        return eventMapper.toEventFullDto(event, getConfirmedRequests(id), getViews(id));
    }


    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        if (!eventRepository.existsByIdAndInitiatorId(eventId, userId)) {
            throw new NotFoundException("Event not found or user is not initiator");
        }

        return requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatuses(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found or user is not initiator"));

        if (event.getParticipantLimit() > 0 && event.getParticipantLimit() <= getConfirmedRequests(eventId)) {
            throw new ConflictException("Participant limit reached");
        }

        int slotsOnEvent = event.getParticipantLimit() - (int) getConfirmedRequests(eventId);
        if (slotsOnEvent <= 0) {
            throw new ConflictException("Participant limit reached");
        }

        List<ParticipationRequest> requests = requestRepository.findAllByIdInAndEventId(updateRequest.getRequestIds(), eventId);

        if (requests.stream().anyMatch(r -> r.getStatus() != RequestStatus.PENDING)) {
            throw new ConflictException("All requests must be in PENDING status");
        }

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();

        RequestStatus newStatus = RequestStatus.valueOf(updateRequest.getStatus());

        if (newStatus == RequestStatus.CONFIRMED) {
            int availableSlots = event.getParticipantLimit() - (int) getConfirmedRequests(eventId);
            for (ParticipationRequest request : requests) {
                if (availableSlots > 0) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    result.getConfirmedRequests().add(requestMapper.toParticipationRequestDto(request));
                    availableSlots--;
                } else {
                    request.setStatus(RequestStatus.REJECTED);
                    result.getRejectedRequests().add(requestMapper.toParticipationRequestDto(request));
                }
            }
        } else {
            for (ParticipationRequest request : requests) {
                request.setStatus(RequestStatus.REJECTED);
                result.getRejectedRequests().add(requestMapper.toParticipationRequestDto(request));
            }
        }

        requestRepository.saveAll(requests);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getPublicEventsWithFilters(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            EventSort sort,
            Pageable pageable,
            HttpServletRequest request
    ) {
        saveEventViewStats(null, request);

        List<Event> events = eventRepository.findPublicEventsWithFilters(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, pageable
        );


        return events.stream()
                .map(event -> eventMapper.toEventShortDto(event, getConfirmedRequests(event.getId()), getViews(event.getId())))
                .collect(Collectors.toList());
    }


    private long getConfirmedRequests(Long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    private long getViews(Long eventId) {
        try {
            List<ViewStats> stats = statsClient.getStats( // Изменено на statsClient
                    LocalDateTime.now().minusYears(1),
                    LocalDateTime.now(),
                    List.of("/events/" + eventId),
                    true
            );
            return stats != null && !stats.isEmpty() ? stats.get(0).getHits() : 0L;
        } catch (Exception e) {
            log.error("Error getting views for event {}: {}", eventId, e.getMessage());
            return 0;
        }
    }

    private void saveEventViewStats(Long eventId, HttpServletRequest request) {
        try {
            EndpointHit hit = EndpointHit.builder()
                    .app("ewm-main-service")
                    .uri(eventId != null ? "/events/" + eventId : "/events")
                    .ip(request.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build();
            statsClient.saveHit(hit); // Изменено на statsClient
        } catch (Exception e) {
            log.error("Error saving view stats: {}", e.getMessage());
        }
    }

    private void updateEventFields(Event event, UpdateEventUserRequest updateRequest) {
        if (updateRequest.getAnnotation() != null && !updateRequest.getAnnotation().isBlank()) {
            event.setAnnotation(updateRequest.getAnnotation());
        }

        if (updateRequest.getCategory() != null) {
            event.setCategory(categoryRepository.getReferenceById(updateRequest.getCategory()));
        }

        if (updateRequest.getDescription() != null && !updateRequest.getDescription().isBlank()) {
            event.setDescription(updateRequest.getDescription());
        }

        if (updateRequest.getEventDate() != null) {
            event.setEventDate(updateRequest.getEventDate());
        }

        if (updateRequest.getLocation() != null) {
            event.setLocation(updateRequest.getLocation());
        }

        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }

        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }

        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }

        if (updateRequest.getTitle() != null && !updateRequest.getTitle().isBlank()) {
            event.setTitle(updateRequest.getTitle());
        }
    }

    private void updateEventFields(Event event, UpdateEventAdminRequest updateRequest) {
        if (updateRequest.getAnnotation() != null && !updateRequest.getAnnotation().isBlank()) {
            event.setAnnotation(updateRequest.getAnnotation());
        }

        if (updateRequest.getCategory() != null) {
            event.setCategory(categoryRepository.getReferenceById(updateRequest.getCategory()));
        }

        if (updateRequest.getDescription() != null && !updateRequest.getDescription().isBlank()) {
            event.setDescription(updateRequest.getDescription());
        }

        if (updateRequest.getEventDate() != null) {
            event.setEventDate(updateRequest.getEventDate());
        }

        if (updateRequest.getLocation() != null) {
            event.setLocation(updateRequest.getLocation());
        }

        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }

        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }

        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }

        if (updateRequest.getTitle() != null && !updateRequest.getTitle().isBlank()) {
            event.setTitle(updateRequest.getTitle());
        }
    }

}
