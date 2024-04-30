package ru.practicum.ewm;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.client.stats.StatsClient;
import ru.practicum.ewm.dto.stats.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final StatsClient statsClient;

    @PostMapping("/hit")
    public void addTestHits() {
        statsClient.saveHitAsync("app", "/service-a", "0.0.0.1",
                LocalDateTime.of(2024,4,28,10,0));
        statsClient.saveHitAsync("app", "/service-b", "0.0.0.1",
                LocalDateTime.of(2024,4,28,10,0));
        statsClient.saveHitAsync("app", "/service-c", "0.0.0.1",
                LocalDateTime.of(2024,4,28,10,0));
    }

    @GetMapping("/stats")
    public List<ViewStats> getStats() {
        return statsClient.getStats(LocalDateTime.of(2024, 4, 28, 0, 0),
                LocalDateTime.of(2024, 4, 28, 23, 59),
                List.of("/service-a", "/service-b"),
                false);
    }

}
