package ru.practicum.ewm.stats.mapping;

import org.mapstruct.Mapper;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.stats.model.Hit;

@Mapper(componentModel = "spring")
public interface HitMapper {

    Hit toHit(EndpointHit endpointHit);

    EndpointHit toEndpointHit(Hit hit);
}
