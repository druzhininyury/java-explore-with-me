package ru.practicum.ewm.like.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.like.dto.LikeDto;
import ru.practicum.ewm.like.model.Like;

@Mapper(componentModel = "spring")
public interface LikeMapper {

    @Mapping(target = "eventId", source = "like.event.id")
    @Mapping(target = "isPositive", source = "like.positive")
    @Mapping(target = "userId", source = "like.user.id")
    LikeDto toLikeDto(Like like);

}
