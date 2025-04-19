package ru.practicum.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;
import ru.practicum.stats.mapper.EndpointHitMapperImpl;
import ru.practicum.stats.service.StatsServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
@Import({StatsServiceImpl.class, EndpointHitMapperImpl.class})
@Sql(scripts = "classpath:test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class StatsServiceImplTest {
    @Autowired
    private StatsServiceImpl statsService;

    @Test
    void saveHit_shouldSaveEntity() {
        EndpointHit hit = new EndpointHit();
        hit.setApp("test-app");
        hit.setUri("/test");
        hit.setIp("127.0.0.1");
        hit.setTimestamp(LocalDateTime.now());

        statsService.saveHit(hit);

        List<ViewStats> stats = statsService.getStats(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                List.of("/test"),
                false
        );

        assertEquals(1, stats.size());
        assertEquals("/test", stats.get(0).getUri());
    }

    @Test
    void getStats_shouldReturnCorrectCount() {
        List<ViewStats> stats = statsService.getStats(
                LocalDateTime.of(2023, 1, 1, 0, 0),
                LocalDateTime.of(2023, 12, 31, 23, 59),
                List.of("/events/1"),
                false
        );

        assertEquals(1, stats.size());
        assertEquals(2L, stats.get(0).getHits());
    }

    @Test
    void getStats_unique_shouldReturnUniqueCount() {
        List<ViewStats> stats = statsService.getStats(
                LocalDateTime.of(2023, 1, 1, 0, 0),
                LocalDateTime.of(2023, 12, 31, 23, 59),
                List.of("/events/1"),
                true
        );

        assertEquals(1, stats.size());
        assertEquals(1L, stats.get(0).getHits()); // В тестовых данных 2 хита с одинаковым IP
    }
}