package ru.practicum.stats_client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsClientImpl implements StatsClient {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    private final RestTemplate restTemplate;

    @Value("${stats.client.base-url:http://localhost:9090}")
    private String serverUrl;

    @Override
    public void saveHit(EndpointHit hit) {
        try {
            restTemplate.postForEntity(
                    serverUrl + "/hit",
                    hit,
                    Void.class
            );
            log.info("Hit successfully saved: {}", hit);
        } catch (HttpClientErrorException e) {
            log.error("Error saving hit: {}", e.getMessage());
            throw new StatsClientException("Error saving hit: " + e.getMessage());
        }
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                                    List<String> uris, boolean unique) {
        try {
            // Создаем URI с параметрами без ручного кодирования
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl + "/stats")
                    .queryParam("start", start.format(DATE_TIME_FORMATTER))
                    .queryParam("end", end.format(DATE_TIME_FORMATTER))
                    .queryParam("unique", unique);

            if (uris != null && !uris.isEmpty()) {
                builder.queryParam("uris", String.join(",", uris));
            }

            // Используем URI объект вместо строки
            URI uri = builder.build().toUri();
            log.debug("Requesting stats with URI: {}", uri);

            ResponseEntity<List<ViewStats>> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Error getting stats: {}", e.getMessage());
            throw new StatsClientException("Error getting stats: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error getting stats: {}", e.getMessage());
            throw new StatsClientException("Unexpected error getting stats: " + e.getMessage());
        }
    }
}