package ru.practicum.ewm.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.stats.exception.EntityHasNotSavedException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StatsController.class)
public class StatsControllerTest {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    MockMvc mvc;

    @MockBean
    StatsService statsService;

    @Test
    void addHitTest_whenHitValid_thenSave() throws Exception {
        EndpointHit newHit = EndpointHit.builder().app("app").uri("/service").ip("192.0.0.1")
                .timestamp(LocalDateTime.of(2024, 4, 1, 12, 0)).build();
        EndpointHit responseHit = EndpointHit.builder().id(1L).app("app").uri("/service").ip("192.0.0.1")
                .timestamp(LocalDateTime.of(2024, 4, 1, 12, 0)).build();

        when(statsService.saveHit(newHit)).thenReturn(responseHit);

        mvc.perform(post("/hit")
                    .content(mapper.writeValueAsString(newHit))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(responseHit.getId()), Long.class))
                .andExpect(jsonPath("$.app", is(responseHit.getApp())))
                .andExpect(jsonPath("$.uri", is(responseHit.getUri())))
                .andExpect(jsonPath("$.ip", is(responseHit.getIp())))
                .andExpect(jsonPath("$.timestamp", is(responseHit.getTimestamp()
                        .format(DateTimeFormatter.ofPattern(EndpointHit.DATE_TIME_FORMAT)))));
    }

    @Test
    void addHitTest_whenDatabaseError_thenExceptionThrow() throws Exception {
        EndpointHit newHit = EndpointHit.builder().app("app").uri("/service").ip("192.0.0.1")
                .timestamp(LocalDateTime.of(2024, 4, 1, 12, 0)).build();

        when(statsService.saveHit(newHit)).thenThrow(new EntityHasNotSavedException("Error"));

        mvc.perform(post("/hit")
                    .content(mapper.writeValueAsString(newHit))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void getStatsTest_whenRequestValid_thenReturnStats() throws Exception {
        LocalDateTime start = LocalDateTime.of(2024, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 4, 1, 23, 59);
        List<String> uris = List.of("/service");
        boolean unique = false;

        ViewStats stats = ViewStats.builder().app("app").uri("/service").hits(10L).build();

        when(statsService.getStats(start, end, uris, unique)).thenReturn(List.of(stats));

        mvc.perform(get("/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                        start.format(DateTimeFormatter.ofPattern(EndpointHit.DATE_TIME_FORMAT)),
                        end.format(DateTimeFormatter.ofPattern(EndpointHit.DATE_TIME_FORMAT)),
                        uris.get(0),
                        unique)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app", is(stats.getApp())))
                .andExpect(jsonPath("$[0].uri", is(stats.getUri())))
                .andExpect(jsonPath("$[0].hits", is(stats.getHits()), Long.class));
    }

}
