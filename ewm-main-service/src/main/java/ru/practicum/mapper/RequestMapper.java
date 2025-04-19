package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.participationRequest.EventRequestStatusUpdateResult;
import ru.practicum.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.model.ParticipationRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RequestMapper {
    @Mapping(target = "event", source = "event.id")
    @Mapping(target = "requester", source = "requester.id")
    ParticipationRequestDto toParticipationRequestDto(ParticipationRequest participationRequest);

    EventRequestStatusUpdateResult toEventRequestStatusUpdateResult(
            List<ParticipationRequest> confirmedRequests,
            List<ParticipationRequest> rejectedRequests
    );
}