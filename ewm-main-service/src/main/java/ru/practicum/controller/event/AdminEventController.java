package ru.practicum.controller.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
public class AdminEventController {

    private final EventService eventService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping
    public List<EventFullDto> searchEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<EventState> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(defaultValue = "0") @Min(0) @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Min(1) @Positive int size) {


        LocalDateTime parsedRangeStart = parseDateTime(rangeStart);
        LocalDateTime parsedRangeEnd = parseDateTime(rangeEnd);

        return eventService.searchEvents(
                users,
                states,
                categories,
                parsedRangeStart,
                parsedRangeEnd,
                from,
                size
        );
    }

    private LocalDateTime parseDateTime(String dateTime) {
        if (dateTime == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTime, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid datetime format. Expected: yyyy-MM-dd HH:mm:ss");
        }
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