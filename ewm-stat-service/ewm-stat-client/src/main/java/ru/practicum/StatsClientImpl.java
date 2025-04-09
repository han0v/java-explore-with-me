package ru.practicum;

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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsClientImpl implements StatsClient {
    private final RestTemplate restTemplate;

    @Value("${stats-server.url:http://localhost:9090}")
    private String serverUrl;

    @Override
    public void saveHit(EndpointHit hit) {
        try {
            restTemplate.postForEntity(
                    serverUrl + "/hit",
                    hit,
                    Void.class
            );
        } catch (HttpClientErrorException e) {
            log.error("Ошибка при сохранении статистики: {}", e.getMessage());
            throw new StatsClientException("Ошибка сохранения hit");
        }
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                                    List<String> uris, boolean unique) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(serverUrl + "/stats")
                    .queryParam("start", formatDateTime(start))
                    .queryParam("end", formatDateTime(end))
                    .queryParam("unique", unique);

            if (uris != null && !uris.isEmpty()) {
                builder.queryParam("uris", String.join(",", uris));
            }

            ResponseEntity<List<ViewStats>> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Ошибка при получении статистики: {}", e.getMessage());
            throw new StatsClientException("Ошибка получения stats");
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}