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
import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;
import ru.practicum.stats.service.StatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
            @NotNull
            String start,

            @RequestParam
            @DateTimeFormat(pattern = DATE_TIME_PATTERN)
            @NotNull
            String end,

            @RequestParam(required = false)
            List<String> uris,

            @RequestParam(defaultValue = "false")
            Boolean unique) {

        LocalDateTime startDateTime = LocalDateTime.parse(start, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
        LocalDateTime endDateTime = LocalDateTime.parse(end, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));

        return ResponseEntity.ok(statsService.getStats(startDateTime, endDateTime, uris, unique));
    }
}