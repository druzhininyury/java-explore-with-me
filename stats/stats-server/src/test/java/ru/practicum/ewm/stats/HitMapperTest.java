package ru.practicum.ewm.stats;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.stats.mapping.HitMapper;
import ru.practicum.ewm.stats.model.Hit;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class HitMapperTest {

    HitMapper hitMapper = Mappers.getMapper(HitMapper.class);

    @Test
    void toHitTest() {
        EndpointHit endpointHitWithNoId = EndpointHit.builder().app("app").uri("/service").ip("0.0.0.1")
                .timestamp(LocalDateTime.of(2024, 4, 28, 10, 0)).build();
        EndpointHit endpointHitWithId = EndpointHit.builder().id(1L).app("app").uri("/service").ip("0.0.0.1")
                .timestamp(LocalDateTime.of(2024, 4, 28, 10, 0)).build();

        Hit expectedHitWithNoId = Hit.builder().id(0L).app("app").uri("/service").ip("0.0.0.1")
                .timestamp(LocalDateTime.of(2024, 4, 28, 10, 0)).build();
        Hit expectedHitWithId = Hit.builder().id(1L).app("app").uri("/service").ip("0.0.0.1")
                .timestamp(LocalDateTime.of(2024, 4, 28, 10, 0)).build();

        Hit actualHitWithNoId = hitMapper.toHit(endpointHitWithNoId);
        assertThat(actualHitWithNoId.getId(), equalTo(expectedHitWithNoId.getId()));
        assertThat(actualHitWithNoId.getApp(), equalTo(expectedHitWithNoId.getApp()));
        assertThat(actualHitWithNoId.getUri(), equalTo(expectedHitWithNoId.getUri()));
        assertThat(actualHitWithNoId.getIp(), equalTo(expectedHitWithNoId.getIp()));
        assertThat(actualHitWithNoId.getTimestamp(), equalTo(expectedHitWithNoId.getTimestamp()));

        Hit actualHitWithId = hitMapper.toHit(endpointHitWithId);
        assertThat(actualHitWithId.getId(), equalTo(expectedHitWithId.getId()));
        assertThat(actualHitWithId.getApp(), equalTo(expectedHitWithId.getApp()));
        assertThat(actualHitWithId.getUri(), equalTo(expectedHitWithId.getUri()));
        assertThat(actualHitWithId.getIp(), equalTo(expectedHitWithId.getIp()));
        assertThat(actualHitWithId.getTimestamp(), equalTo(expectedHitWithId.getTimestamp()));
    }

    @Test
    void toEndpointHitTest() {
        Hit hit = Hit.builder().id(1L).app("app").uri("/service").ip("0.0.0.1")
                .timestamp(LocalDateTime.of(2024, 4, 28, 10, 0)).build();

        EndpointHit expectedEndpointHit = EndpointHit.builder().id(1L).app("app").uri("/service").ip("0.0.0.1")
                .timestamp(LocalDateTime.of(2024, 4, 28, 10, 0)).build();

        EndpointHit actualEndpointHit = hitMapper.toEndpointHit(hit);
        assertThat(actualEndpointHit, equalTo(expectedEndpointHit));
    }

}
