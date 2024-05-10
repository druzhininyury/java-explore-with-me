package ru.practicum.ewm.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.stats.exception.EntityHasNotSavedException;
import ru.practicum.ewm.stats.mapping.HitMapper;
import ru.practicum.ewm.stats.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {

    private final HitMapper hitMapper;
    private final StatsRepository statsRepository;

    @Override
    @Transactional
    public EndpointHit saveHit(EndpointHit endpointHit) {
        try {
            Hit hit = statsRepository.save(hitMapper.toHit(endpointHit));
            log.info("Added hit: {}", hit);
            return hitMapper.toEndpointHit(hit);
        } catch (DataIntegrityViolationException e) {
            throw new EntityHasNotSavedException("Hit hasn't been saved: " + endpointHit);
        }
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        List<ViewStats> stats = unique ? statsRepository.getStatsUniqueWithFilters(start, end, uris) :
                statsRepository.getStatsWithFilters(start, end, uris);
        log.info("Stats sent for request: start={} end={} uris={} unique={}", start, end, uris, unique);
        return stats;
    }

}
