package ru.practicum.ewm.event.service;

import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.model.Event;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    int PUBLISHING_MIN_DELAY = 1;

    EventFullDto updateEvent(long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventFullDto> getEvents(List<Long> usersIds,
                                 List<Event.State> eventsStates,
                                 List<Long> categoriesIds,
                                 LocalDateTime rangeStart,
                                 LocalDateTime rangeEnd,
                                 int from,
                                 int size);

    EventFullDto addEvent(long userId, NewEventDto newEventDto);

    EventFullDto updateEvent(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest);

    EventFullDto getEvent(long userId, long eventId);

    List<EventShortDto> getEvents(long userId, int from, int size);

    EventFullDto getEvent(long eventId, HttpServletRequest request);

    List<EventShortDto> getEvents(String text,
                                  List<Long> categoriesIds,
                                  Boolean paid,
                                  LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd,
                                  Boolean onlyAvailable,
                                  Event.SearchSort sort,
                                  int from,
                                  int size,
                                  HttpServletRequest request);

    Long getEventViewsNumber(Long eventId);

    // Заглушка
    List<EventShortDto> loadShortEventsViewsNumber(List<EventShortDto> dtos);

    // Заглушка
    List<EventFullDto> loadFullEventsViewsNumber(List<EventFullDto> dtos);

}
