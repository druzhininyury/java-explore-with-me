package ru.practicum.ewm.stats;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.stats.exception.EntityHasNotSavedException;
import ru.practicum.ewm.stats.mapping.HitMapper;
import ru.practicum.ewm.stats.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StatsServiceImplTest {

    @Spy
    HitMapper hitMapper = Mappers.getMapper(HitMapper.class);
    @Mock
    StatsRepository statsRepository;

    @InjectMocks
    StatsServiceImpl statsService;

    @Test
    void saveHit_whenInputValid_thenSave() {
        EndpointHit newEndpointHit = EndpointHit.builder().app("app").uri("/service").ip("192.0.0.1")
                .timestamp(LocalDateTime.of(2024, 4, 1, 12, 0)).build();
        Hit hit = Hit.builder().id(1L).app("app").uri("/service").ip("192.0.0.1")
                .timestamp(LocalDateTime.of(2024, 4, 1, 12, 0)).build();
        EndpointHit expectedEndpointHit = EndpointHit.builder().id(1L).app("app").uri("/service").ip("192.0.0.1")
                .timestamp(LocalDateTime.of(2024, 4, 1, 12, 0)).build();

        when(statsRepository.save(any(Hit.class))).thenReturn(hit);

        EndpointHit actualResponseHit = statsService.saveHit(newEndpointHit);

        assertThat(actualResponseHit, equalTo(expectedEndpointHit));
        verify(statsRepository, times(1)).save(any(Hit.class));
    }

    @Test
    void saveHit_whenDatabaseError_thenThrowException() {
        EndpointHit newEndpointHit = EndpointHit.builder().app("app").uri("/service").ip("192.0.0.1")
                .timestamp(LocalDateTime.of(2024, 4, 1, 12, 0)).build();

        when(statsRepository.save(any(Hit.class))).thenThrow(new DataIntegrityViolationException("Database error."));

        assertThrows(EntityHasNotSavedException.class, () -> statsService.saveHit(newEndpointHit));
    }

    @Test
    void getStats_whenInputValid_thenReturnListOfDto() {
        LocalDateTime start = LocalDateTime.of(2024, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 4, 1, 23, 59);

        ViewStats stats = ViewStats.builder().app("app").uri("/service").hits(10L).build();

        when(statsRepository.getStatsWithFilters(start, end, null)).thenReturn(List.of(stats));

        List<ViewStats> actualStatsList = statsService.getStats(start, end, null, false);

        assertThat(actualStatsList, equalTo(List.of(stats)));
        verify(statsRepository).getStatsWithFilters(start, end, null);
    }

    @Test
    void getStats_whenInputWithUnique_thenReturnListOfDto() {
        LocalDateTime start = LocalDateTime.of(2024, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 4, 1, 23, 59);

        ViewStats stats = ViewStats.builder().app("app").uri("/service").hits(10L).build();

        when(statsRepository.getStatsUniqueWithFilters(start, end, null)).thenReturn(List.of(stats));

        List<ViewStats> actualStatsList = statsService.getStats(start, end, null, true);

        assertThat(actualStatsList, equalTo(List.of(stats)));
        verify(statsRepository).getStatsUniqueWithFilters(start, end, null);
    }

    @Test
    void getStats_whenInputWithUris_thenReturnListOfDto() {
        LocalDateTime start = LocalDateTime.of(2024, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 4, 1, 23, 59);
        List<String> uris = List.of("/service");

        ViewStats stats = ViewStats.builder().app("app").uri("/service").hits(10L).build();

        when(statsRepository.getStatsWithFilters(start, end, uris)).thenReturn(List.of(stats));

        List<ViewStats> actualStatsList = statsService.getStats(start, end, uris, false);

        assertThat(actualStatsList, equalTo(List.of(stats)));
        verify(statsRepository).getStatsWithFilters(start, end, uris);
    }

    @Test
    void getStats_whenInputWithUrisAndUnique_thenReturnListOfDto() {
        LocalDateTime start = LocalDateTime.of(2024, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 4, 1, 23, 59);
        List<String> uris = List.of("/service");

        ViewStats stats = ViewStats.builder().app("app").uri("/service").hits(10L).build();

        when(statsRepository.getStatsUniqueWithFilters(start, end, uris)).thenReturn(List.of(stats));

        List<ViewStats> actualStatsList = statsService.getStats(start, end, uris, true);

        assertThat(actualStatsList, equalTo(List.of(stats)));
        verify(statsRepository).getStatsUniqueWithFilters(start, end, uris);
    }
}
