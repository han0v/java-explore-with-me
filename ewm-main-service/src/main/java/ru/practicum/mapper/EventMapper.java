package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.practicum.dto.event.*;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CategoryMapper.class})
public interface EventMapper {
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", expression = "java(ru.practicum.model.EventState.PENDING)")
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())") // добавляем это
    Event toEvent(NewEventDto newEventDto);


    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "views", source = "views")
    EventFullDto toEventFullDto(Event event, Long confirmedRequests, Long views);

    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "views", source = "views")
    EventShortDto toEventShortDto(Event event, Long confirmedRequests, Long views);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "eventDate", ignore = true)
    void updateEventFromAdminDto(UpdateEventAdminRequest dto, @MappingTarget Event event);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "eventDate", ignore = true)
    void updateEventFromUserDto(UpdateEventUserRequest dto, @MappingTarget Event event);
}