package ru.practicum.service.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.*;
import ru.practicum.repository.event.EventRepository;
import ru.practicum.repository.request.RequestRepository;
import ru.practicum.repository.user.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;

    LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot create request");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Event is not published");
        }


        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.PENDING);
        if (event.getParticipantLimit() > 0 && confirmed >= event.getParticipantLimit()) {
            throw new ConflictException("Participant limit reached");
        }

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Request already exists");
        }

        boolean autoConfirm = event.getParticipantLimit() == 0;
        RequestStatus status = autoConfirm ? RequestStatus.CONFIRMED : RequestStatus.PENDING;

        ParticipationRequest request = ParticipationRequest.builder()
                .requester(user)
                .event(event)
                .created(now)
                .status(status)
                .build();

        return requestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        if (request.getStatus() == RequestStatus.CONFIRMED) {
            throw new ConflictException("Cannot cancel confirmed request");
        }

        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Request does not belong to user");
        }
        request.setStatus(RequestStatus.CANCELED);
        return requestMapper.toParticipationRequestDto(requestRepository.save(request));
    }
}

