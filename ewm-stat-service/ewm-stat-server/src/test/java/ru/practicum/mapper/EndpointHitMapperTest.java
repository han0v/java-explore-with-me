package ru.practicum.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.EndpointHit;
import ru.practicum.stats.mapper.EndpointHitMapper;
import ru.practicum.stats.model.EndpointHitEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class EndpointHitMapperTest {
    private final EndpointHitMapper mapper = Mappers.getMapper(EndpointHitMapper.class);

    @Test
    void toEntity_shouldMapCorrectly() {
        EndpointHit hit = new EndpointHit();
        hit.setApp("test-app");
        hit.setUri("/test");
        hit.setIp("127.0.0.1");
        hit.setTimestamp(LocalDateTime.now());

        EndpointHitEntity entity = mapper.toEntity(hit);

        assertNull(entity.getId());
        assertEquals(hit.getApp(), entity.getApp());
        assertEquals(hit.getUri(), entity.getUri());
        assertEquals(hit.getIp(), entity.getIp());
        assertEquals(hit.getTimestamp(), entity.getTimestamp());
    }

    @Test
    void toDto_shouldMapCorrectly() {
        EndpointHitEntity entity = new EndpointHitEntity();
        entity.setId(1L);
        entity.setApp("test-app");
        entity.setUri("/test");
        entity.setIp("127.0.0.1");
        entity.setTimestamp(LocalDateTime.now());

        EndpointHit dto = mapper.toDto(entity);

        assertEquals(entity.getApp(), dto.getApp());
        assertEquals(entity.getUri(), dto.getUri());
        assertEquals(entity.getIp(), dto.getIp());
        assertEquals(entity.getTimestamp(), dto.getTimestamp());
    }
}