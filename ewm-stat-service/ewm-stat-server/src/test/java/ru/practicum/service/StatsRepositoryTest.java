package ru.practicum.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.ViewStats;
import ru.practicum.model.EndpointHitEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)

public class StatsRepositoryTest {
    @Autowired
    private StatsRepository statsRepository;

    @Test
    void getStats_shouldReturnNonEmptyList() {
        List<ViewStats> stats = statsRepository.getStats(
                LocalDateTime.of(2023, 1, 1, 0, 0),
                LocalDateTime.of(2023, 12, 31, 23, 59),
                List.of("/events/1")
        );

        assertFalse(stats.isEmpty());
        assertEquals("/events/1", stats.get(0).getUri());
    }

    @Test
    void getUniqueStats_shouldReturnUniqueCount() {
        List<ViewStats> stats = statsRepository.getUniqueStats(
                LocalDateTime.of(2023, 1, 1, 0, 0),
                LocalDateTime.of(2023, 12, 31, 23, 59),
                List.of("/events/1")
        );

        assertEquals(1, stats.size());
        assertEquals(1L, stats.get(0).getHits()); // В тестовых данных 2 хита с одинаковым IP
    }

    @Test
    void save_shouldPersistEntity() {
        EndpointHitEntity entity = new EndpointHitEntity();
        entity.setApp("test-app");
        entity.setUri("/test");
        entity.setIp("127.0.0.1");
        entity.setTimestamp(LocalDateTime.now());

        EndpointHitEntity saved = statsRepository.save(entity);

        assertNotNull(saved.getId());
        assertEquals(entity.getApp(), saved.getApp());
    }
}