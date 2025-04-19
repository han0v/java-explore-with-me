package ru.practicum.stats.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.EndpointHit;
import ru.practicum.stats.model.EndpointHitEntity;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface EndpointHitMapper {
    @Mapping(target = "id", ignore = true)
    EndpointHitEntity toEntity(EndpointHit dto);

    @Mapping(source = "timestamp", target = "timestamp")
    EndpointHit toDto(EndpointHitEntity entity);

    default LocalDateTime map(Timestamp timestamp) {
        return timestamp.toLocalDateTime();
    }
}