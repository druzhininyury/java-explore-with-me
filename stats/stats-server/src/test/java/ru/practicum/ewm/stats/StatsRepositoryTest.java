package ru.practicum.ewm.stats;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.stats.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DataJpaTest
public class StatsRepositoryTest {

    @Autowired
    StatsRepository statsRepository;

    @BeforeEach
    void fillDatabase() {
        statsRepository.save(Hit.builder().app("app").uri("/service-a").ip("0.0.0.1")
                .timestamp(LocalDateTime.of(2024, 4, 28, 10, 0)).build());
        statsRepository.save(Hit.builder().app("app").uri("/service-a").ip("0.0.0.2")
                .timestamp(LocalDateTime.of(2024, 4, 28, 11, 0)).build());
        statsRepository.save(Hit.builder().app("app").uri("/service-a").ip("0.0.0.2")
                .timestamp(LocalDateTime.of(2024, 4, 28, 12, 0)).build());
        statsRepository.save(Hit.builder().app("app").uri("/service-a").ip("0.0.0.3")
                .timestamp(LocalDateTime.of(2024, 4, 28, 13, 0)).build());
        statsRepository.save(Hit.builder().app("app").uri("/service-b").ip("0.0.0.2")
                .timestamp(LocalDateTime.of(2024, 4, 28, 14, 0)).build());
        statsRepository.save(Hit.builder().app("app").uri("/service-b").ip("0.0.0.3")
                .timestamp(LocalDateTime.of(2024, 4, 28, 15, 0)).build());
    }

    @AfterEach
    void emptyDatabase() {
        statsRepository.deleteAll();
    }

    @Test
    void getStatsTest() {
        LocalDateTime start = LocalDateTime.of(2024, 4, 28, 11, 0);
        LocalDateTime end = LocalDateTime.of(2024, 4, 28, 14, 0);

        List<ViewStats> expectedStats = List.of(ViewStats.builder().app("app").uri("/service-a").hits(3L).build(),
                ViewStats.builder().app("app").uri("/service-b").hits(1L).build());

        List<ViewStats> actualStats = statsRepository.getStats(start, end);

        assertThat(actualStats, equalTo(expectedStats));
    }

    @Test
    void getStatsUniqueTest() {
        LocalDateTime start = LocalDateTime.of(2024, 4, 28, 11, 0);
        LocalDateTime end = LocalDateTime.of(2024, 4, 28, 14, 0);

        List<ViewStats> expectedStats = List.of(ViewStats.builder().app("app").uri("/service-a").hits(2L).build(),
                ViewStats.builder().app("app").uri("/service-b").hits(1L).build());

        List<ViewStats> actualStats = statsRepository.getStatsUnique(start, end);

        assertThat(actualStats, equalTo(expectedStats));
    }

    @Test
    void getStatsWithUrisTest() {
        LocalDateTime start = LocalDateTime.of(2024, 4, 28, 11, 0);
        LocalDateTime end = LocalDateTime.of(2024, 4, 28, 14, 0);
        List<String> uris = List.of("/service-a");

        List<ViewStats> expectedStats = List.of(ViewStats.builder().app("app").uri("/service-a").hits(3L).build());

        List<ViewStats> actualStats = statsRepository.getStatsWithUris(start, end, uris);

        assertThat(actualStats, equalTo(expectedStats));
    }

    @Test
    void getStatsUniqueWithUrisTest() {
        LocalDateTime start = LocalDateTime.of(2024, 4, 28, 11, 0);
        LocalDateTime end = LocalDateTime.of(2024, 4, 28, 14, 0);
        List<String> uris = List.of("/service-a");

        List<ViewStats> expectedStats = List.of(ViewStats.builder().app("app").uri("/service-a").hits(2L).build());

        List<ViewStats> actualStats = statsRepository.getStatsUniqueWithUris(start, end, uris);

        assertThat(actualStats, equalTo(expectedStats));
    }

}
