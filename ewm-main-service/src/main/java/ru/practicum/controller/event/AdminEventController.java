package ru.practicum.controller.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
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
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(defaultValue = "0") @Min(0) @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Min(1) @Positive int size) {

        List<Long> filteredUsers = (users == null || users.isEmpty()) ? null : users;
        List<String> filteredStates = (states == null || states.isEmpty()) ? null : states;
        List<Long> filteredCategories = (categories == null || categories.isEmpty()) ? null : categories;

        LocalDateTime startDateTime = parseDateTime(rangeStart);
        LocalDateTime endDateTime = parseDateTime(rangeEnd);

        if (startDateTime != null && endDateTime != null && startDateTime.isAfter(endDateTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Дата начала диапазона должна быть раньше даты окончания");
        }

        return eventService.searchEvents(
                filteredUsers,
                filteredStates,
                filteredCategories,
                startDateTime,
                endDateTime,
                from,
                size
        );
    }

    private LocalDateTime parseDateTime(String dateTime) {
        if (dateTime == null || dateTime.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTime, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Некорректный формат даты. Используйте yyyy-MM-dd HH:mm:ss");
        }
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long eventId,
                                    @Valid @RequestBody UpdateEventAdminRequest request) {
        if (request.getEventDate() != null && request.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Дата события не может быть в прошлом");
        }
        return eventService.updateEventByAdmin(eventId, request);
    }
}
