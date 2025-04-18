package ru.practicum.controller.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.model.EventState;
import ru.practicum.service.event.EventService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
public class AdminEventController {

    private final EventService eventService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping
    public List<EventFullDto> searchEvents(@RequestParam(required = false) List<Long> users,
                                           @RequestParam(required = false) List<EventState> states,
                                           @RequestParam(required = false) List<Long> categories,
                                           @RequestParam(required = false) String rangeStart,
                                           @RequestParam(required = false) String rangeEnd,
                                           @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                           @RequestParam(defaultValue = "10") @Positive int size) {
        LocalDateTime parsedRangeStart = null;
        LocalDateTime parsedRangeEnd = null;

        if (rangeStart != null) {
            try {
                parsedRangeStart = LocalDateTime.parse(rangeStart, FORMATTER);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid format for rangeStart. Expected format: yyyy-MM-dd HH:mm:ss");
            }
        }

        if (rangeEnd != null) {
            try {
                parsedRangeEnd = LocalDateTime.parse(rangeEnd, FORMATTER);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid format for rangeEnd. Expected format: yyyy-MM-dd HH:mm:ss");
            }
        }

        return eventService.searchEvents(
                users != null ? users : Collections.emptyList(),
                states != null ? states : Collections.emptyList(),
                categories != null ? categories : Collections.emptyList(),
                parsedRangeStart,
                parsedRangeEnd,
                from,
                size
        );
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long eventId,
                                    @Valid @RequestBody UpdateEventAdminRequest request) {
        if (request.getEventDate() != null && request.getEventDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Event date cannot be in the past");
        }
        return eventService.updateEventByAdmin(eventId, request);
    }
}