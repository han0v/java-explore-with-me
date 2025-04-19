package ru.practicum.service.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.*;
import ru.practicum.repository.event.EventRepository;
import ru.practicum.repository.request.RequestRepository;
import ru.practicum.repository.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RequestMapper requestMapper;

    @InjectMocks
    private RequestServiceImpl requestService;

    private final User testUser = new User(1L, "user@example.com", "User Name");
    private final User testInitiator = new User(2L, "initiator@example.com", "Initiator");
    private final Event testEvent = Event.builder()
            .id(1L)
            .title("Test Event")
            .annotation("Test Annotation")
            .description("Test Description")
            .eventDate(LocalDateTime.now().plusDays(1))
            .paid(false)
            .participantLimit(10)
            .requestModeration(true)
            .state(EventState.PUBLISHED)
            .initiator(testInitiator)
            .build();
    private final ParticipationRequest testRequest = ParticipationRequest.builder()
            .id(1L)
            .requester(testUser)
            .event(testEvent)
            .created(LocalDateTime.now())
            .status(RequestStatus.PENDING)
            .build();
    private final ParticipationRequestDto testRequestDto = new ParticipationRequestDto(
            1L, LocalDateTime.now(), 1L, 1L, "PENDING");

    @Test
    void getUserRequests_shouldReturnUserRequests() {
        when(requestRepository.findAllByRequesterId(1L)).thenReturn(List.of(testRequest));
        when(requestMapper.toParticipationRequestDto(testRequest)).thenReturn(testRequestDto);

        List<ParticipationRequestDto> result = requestService.getUserRequests(1L);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testRequestDto, result.get(0));
    }

    @Test
    void createRequest_shouldCreateNewRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.PENDING)).thenReturn(5L);
        when(requestRepository.existsByRequesterIdAndEventId(1L, 1L)).thenReturn(false);
        when(requestMapper.toParticipationRequestDto(any())).thenReturn(testRequestDto);

        ParticipationRequestDto result = requestService.createRequest(1L, 1L);

        assertNotNull(result);
        verify(requestRepository).save(any());
    }

    @Test
    void createRequest_shouldThrowConflictForInitiator() {
        testEvent.setInitiator(testUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        assertThrows(ConflictException.class, () -> requestService.createRequest(1L, 1L));
    }

    @Test
    void createRequest_shouldThrowConflictForUnpublishedEvent() {
        testEvent.setState(EventState.PENDING);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        assertThrows(ConflictException.class, () -> requestService.createRequest(1L, 1L));
    }

    @Test
    void createRequest_shouldThrowConflictWhenLimitReached() {
        testEvent.setParticipantLimit(5);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.PENDING)).thenReturn(5L);

        assertThrows(ConflictException.class, () -> requestService.createRequest(1L, 1L));
    }

    @Test
    void createRequest_shouldThrowConflictForDuplicateRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(requestRepository.existsByRequesterIdAndEventId(1L, 1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> requestService.createRequest(1L, 1L));
    }

    @Test
    void createRequest_shouldAutoConfirmWhenNoLimit() {
        testEvent.setParticipantLimit(0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(requestRepository.existsByRequesterIdAndEventId(1L, 1L)).thenReturn(false);
        when(requestMapper.toParticipationRequestDto(any())).thenReturn(testRequestDto);

        ParticipationRequestDto result = requestService.createRequest(1L, 1L);

        assertNotNull(result);
        verify(requestRepository).save(argThat(req -> req.getStatus() == RequestStatus.CONFIRMED));
    }

    @Test
    void cancelRequest_shouldCancelRequest() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        when(requestMapper.toParticipationRequestDto(any())).thenReturn(testRequestDto);

        ParticipationRequestDto result = requestService.cancelRequest(1L, 1L);

        assertNotNull(result);
        verify(requestRepository).save(argThat(req -> req.getStatus() == RequestStatus.CANCELED));
    }

    @Test
    void cancelRequest_shouldThrowConflictForConfirmedRequest() {
        testRequest.setStatus(RequestStatus.CONFIRMED);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(testRequest));

        assertThrows(ConflictException.class, () -> requestService.cancelRequest(1L, 1L));
    }

    @Test
    void cancelRequest_shouldThrowNotFoundForWrongUser() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(testRequest));

        assertThrows(NotFoundException.class, () -> requestService.cancelRequest(2L, 1L));
    }

    @Test
    void cancelRequest_shouldThrowNotFoundForInvalidRequest() {
        when(requestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.cancelRequest(1L, 999L));
    }
}