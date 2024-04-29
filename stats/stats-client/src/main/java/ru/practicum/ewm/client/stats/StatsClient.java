package ru.practicum.ewm.client.stats;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class StatsClient {

    private static final ParameterizedTypeReference<List<ViewStats>> VIEW_STATS_LIST_REFERENCE =
            new ParameterizedTypeReference<List<ViewStats>>() {};

    private final WebClient webClient;

    public StatsClient(@Value("${stats-server.url}") String statsServerUrl) {
        /* delete */ log.info("Stats server address: " + statsServerUrl);
        this.webClient = WebClient.builder()
                .baseUrl(statsServerUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public void saveHitAsync(String app, String uri, String ip, LocalDateTime timestamp) {
        Mono<EndpointHit> monoResponse = webClient.post()
                .uri("/hit")
                .bodyValue(EndpointHit.builder().app(app).uri(uri).ip(ip).timestamp(timestamp).build())
                .retrieve()
                .bodyToMono(EndpointHit.class);
        monoResponse.subscribe(endpointHit -> log.info("Hit was send: " + endpointHit));
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        Mono<List<ViewStats>> monoResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stats")
                        .queryParam("start", start.format(DateTimeFormatter.ofPattern(EndpointHit.DATE_TIME_FORMAT)))
                        .queryParam("end", end.format(DateTimeFormatter.ofPattern(EndpointHit.DATE_TIME_FORMAT)))
                        .queryParamIfPresent("uris", Optional.of(uris))
                        .queryParam("unique", unique)
                        .build())
                .retrieve()
                .bodyToMono(VIEW_STATS_LIST_REFERENCE);

        return monoResponse.block();

    }

}
