package ru.practicum.ewm.event.mapping;

import org.mapstruct.*;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.like.dto.LikeDto;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "state", ignore = true)
    Event toEvent(NewEventDto newEventDto, Category category, User initiator, Location location);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "state", source = "updateEventUserRequest.stateAction")
    Event updateEvent(@MappingTarget Event event,
                      UpdateEventUserRequest updateEventUserRequest,
                      Category category,
                      Location location);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "publishedOn", source = "publishedOn")
    @Mapping(target = "state", source = "updateEventAdminRequest.stateAction")
    Event updateEvent(@MappingTarget Event event,
                      UpdateEventAdminRequest updateEventAdminRequest,
                      Category category,
                      Location location,
                      LocalDateTime publishedOn);

    EventFullDto toEventFullDto(Event event, long views);

    @Mapping(target = "rating",
             expression = "java(getEventRating(event.getLikes(), event.getDislikes()))")
    EventShortDto toEventShortDto(Event event);

    default String getEventRating(long likes, long dislikes) {
        long total = likes + dislikes;
        if (total == 0) {
            return "No likes.";
        }
        return LikeDto.LikesLevel.getLevel((int) (likes * 100 / total)).toString();
    }

    List<EventFullDto> toEventFullDto(List<Event> events);

    List<EventShortDto> toEventShortDto(List<Event> events);

    @ValueMapping(target = "PENDING", source = "SEND_TO_REVIEW")
    @ValueMapping(target = "CANCELED", source = "CANCEL_REVIEW")
    Event.State toEventState(UpdateEventUserRequest.State state);

    @ValueMapping(target = "PUBLISHED", source = "PUBLISH_EVENT")
    @ValueMapping(target = "CANCELED", source = "REJECT_EVENT")
    Event.State toEventState(UpdateEventAdminRequest.State state);

}
