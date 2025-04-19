package ru.practicum.stats.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;
import ru.practicum.stats.service.StatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping
@Validated
public class StatsController {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private final StatsService statsService;

    @PostMapping("/hit")
    public ResponseEntity<Void> hit(@Valid @RequestBody EndpointHit endpointHit) {
        statsService.saveHit(endpointHit);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStats>> getStats(
            @RequestParam
            @DateTimeFormat(pattern = DATE_TIME_PATTERN)
            String start,

            @RequestParam
            @DateTimeFormat(pattern = DATE_TIME_PATTERN)
            String end,

            @RequestParam(required = false)
            List<String> uris,

            @RequestParam(defaultValue = "false")
            Boolean unique) {

        if (start == null || end == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start and end parameters are required");
        }

        LocalDateTime startDateTime;
        LocalDateTime endDateTime;
        try {
            startDateTime = LocalDateTime.parse(start, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
            endDateTime = LocalDateTime.parse(end, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format. Expected: " + DATE_TIME_PATTERN);
        }

        if (startDateTime.isAfter(endDateTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date must be before end date");
        }

        return ResponseEntity.ok(statsService.getStats(startDateTime, endDateTime, uris, unique));
    }
}