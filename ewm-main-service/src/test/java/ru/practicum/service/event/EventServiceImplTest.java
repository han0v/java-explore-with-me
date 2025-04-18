package ru.practicum.service.event;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.EndpointHit;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private StatsClient statsClient;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private RequestMapper requestMapper;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private EventServiceImpl eventService;

    private final User testUser = new User(1L, "user@example.com", "User Name");
    private final Category testCategory = new Category(1L, "Category Name");
    private final Event testEvent = Event.builder()
            .id(1L)
            .title("Test Event")
            .annotation("Test Annotation")
            .description("Test Description")
            .eventDate(LocalDateTime.now().plusDays(1))
            .paid(false)
            .participantLimit(10)
            .requestModeration(true)
            .state(EventState.PENDING)
            .initiator(testUser)
            .category(testCategory)
            .createdOn(LocalDateTime.now())
            .build();

    @Test
    void createEvent_shouldCreateNewEvent() {
        NewEventDto newEventDto = new NewEventDto();
        newEventDto.setCategory(1L);
        newEventDto.setTitle("Test Event");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(eventMapper.toEvent(newEventDto)).thenReturn(testEvent);
        when(eventRepository.save(testEvent)).thenReturn(testEvent);
        when(eventMapper.toEventFullDto(testEvent, 0L, 0L)).thenReturn(new EventFullDto());

        EventFullDto result = eventService.createEvent(1L, newEventDto);

        assertNotNull(result);
        verify(eventRepository).save(testEvent);
    }

    @Test
    void createEvent_shouldThrowNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> eventService.createEvent(1L, new NewEventDto()));
    }


    @Test
    void getUserEventById_shouldReturnEvent() {
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(testEvent));
        when(eventMapper.toEventFullDto(any(), anyLong(), anyLong())).thenReturn(new EventFullDto());

        EventFullDto result = eventService.getUserEventById(1L, 1L);

        assertNotNull(result);
    }

    @Test
    void updateEventByUser_shouldUpdateEvent() {
        UpdateEventUserRequest updateRequest = new UpdateEventUserRequest();
        updateRequest.setTitle("Updated Title");

        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any())).thenReturn(testEvent);
        when(eventMapper.toEventFullDto(any(), anyLong(), anyLong())).thenReturn(new EventFullDto());

        EventFullDto result = eventService.updateEventByUser(1L, 1L, updateRequest);

        assertNotNull(result);
    }

    @Test
    void updateEventByUser_shouldThrowConflictExceptionForPublishedEvent() {
        testEvent.setState(EventState.PUBLISHED);

        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(testEvent));

        assertThrows(ConflictException.class,
                () -> eventService.updateEventByUser(1L, 1L, new UpdateEventUserRequest()));
    }

    @Test
    void searchEvents_shouldReturnFilteredEvents() {
        when(eventRepository.findEventsByAdminParams(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(testEvent));
        when(eventMapper.toEventFullDto(any(), anyLong(), anyLong())).thenReturn(new EventFullDto());

        List<EventFullDto> result = eventService.searchEvents(
                List.of(1L), List.of(EventState.PENDING), List.of(1L),
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), 0, 10);

        assertFalse(result.isEmpty());
    }

    @Test
    void updateEventByAdmin_shouldPublishEvent() {
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest();
        updateRequest.setStateAction("PUBLISH_EVENT");

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any())).thenReturn(testEvent);
        when(eventMapper.toEventFullDto(any(), anyLong(), anyLong())).thenReturn(new EventFullDto());

        EventFullDto result = eventService.updateEventByAdmin(1L, updateRequest);

        assertNotNull(result);
        assertEquals(EventState.PUBLISHED, testEvent.getState());
    }

    @Test
    void getPublicEventById_shouldReturnPublishedEvent() {
        testEvent.setState(EventState.PUBLISHED);

        when(eventRepository.findByIdAndState(1L, EventState.PUBLISHED)).thenReturn(Optional.of(testEvent));
        when(eventMapper.toEventFullDto(any(), anyLong(), anyLong())).thenReturn(new EventFullDto());

        EventFullDto result = eventService.getPublicEventById(1L, httpServletRequest);

        assertNotNull(result);
        verify(statsClient).saveHit(any(EndpointHit.class));
    }

    @Test
    void getEventRequests_shouldReturnRequests() {
        when(eventRepository.existsByIdAndInitiatorId(1L, 1L)).thenReturn(true);
        when(requestRepository.findAllByEventId(1L)).thenReturn(List.of());

        List<ParticipationRequestDto> result = eventService.getEventRequests(1L, 1L);

        assertNotNull(result);
    }

    @Test
    void updateRequestStatuses_shouldConfirmRequests() {
        Event event = Event.builder().participantLimit(10).build();
        ParticipationRequest request = new ParticipationRequest();
        request.setStatus(RequestStatus.PENDING);

        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(5L);
        when(requestRepository.findAllByIdInAndEventId(any(), eq(1L))).thenReturn(List.of(request));
        when(requestMapper.toParticipationRequestDto(any())).thenReturn(new ParticipationRequestDto());

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L));
        updateRequest.setStatus("CONFIRMED");

        EventRequestStatusUpdateResult result = eventService.updateRequestStatuses(1L, 1L, updateRequest);

        assertFalse(result.getConfirmedRequests().isEmpty());
        assertTrue(result.getRejectedRequests().isEmpty());
    }

    @Test
    void getPublicEventsWithFilters_shouldReturnFilteredEvents() {
        when(eventRepository.findPublicEventsWithFilters(any(), any(), any(), any(), any(), anyBoolean(), any(), any()))
                .thenReturn(List.of(testEvent));
        when(eventMapper.toEventShortDto(any(), anyLong(), anyLong())).thenReturn(new EventShortDto());

        List<EventShortDto> result = eventService.getPublicEventsWithFilters(
                "text", List.of(1L), false,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                false, EventSort.EVENT_DATE, PageRequest.of(0, 10), httpServletRequest);

        assertFalse(result.isEmpty());
    }

}