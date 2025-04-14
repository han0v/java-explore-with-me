package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;
import ru.practicum.exception.StatsServiceException;
import ru.practicum.mapper.EndpointHitMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;
    private final EndpointHitMapper mapper;

    @Override
    @Transactional
    public void saveHit(EndpointHit hit) {
        try {
            statsRepository.save(mapper.toEntity(hit));
            log.info("Hit saved: {}", hit);
        } catch (DataAccessException e) {
            log.error("Ошибка сохранения hit: {}", e.getMessage());
            throw new StatsServiceException("Ошибка сохранения статистики");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStats> getStats(LocalDateTime start,
                                    LocalDateTime end,
                                    List<String> uris,
                                    boolean unique) {
        return unique
                ? statsRepository.getUniqueStats(start, end, uris)
                : statsRepository.getStats(start, end, uris);
    }
}