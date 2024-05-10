package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.CategoryRepository;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.client.stats.StatsClient;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.mapping.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConditionsException;
import ru.practicum.ewm.exception.EntityNotAccessibleException;
import ru.practicum.ewm.exception.EntityNotFoundException;
import ru.practicum.ewm.user.UserRepository;
import ru.practicum.ewm.user.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventMapper eventMapper;

    private final Environment environment;
    private final StatsClient statsClient;

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    private final LocationService locationService;

    @Override
    @Transactional
    public EventFullDto updateEvent(long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
         Optional<Event> eventContainer = eventRepository.findById(eventId);
        if (eventContainer.isEmpty()) {
            throw new EntityNotFoundException("Event with id=" + eventId + " not found.");
        }
        Event event = eventContainer.get();

        LocalDateTime publishedOn = null;
        if (updateEventAdminRequest.getStateAction() != null &&
                updateEventAdminRequest.getStateAction().equals(UpdateEventAdminRequest.State.PUBLISH_EVENT)) {
            if (!event.getState().equals(Event.State.PENDING)) {
                throw new ConditionsException("Only pending event can be published, but it is " + event.getState());
            }
            publishedOn = LocalDateTime.now();
            if (publishedOn.plusHours(PUBLISHING_MIN_DELAY).compareTo(event.getEventDate()) > 0) {
                throw new ConditionsException("Publishing date must be not later then " + PUBLISHING_MIN_DELAY
                + " hour(s) before than event date.");
            }
        }

        if (updateEventAdminRequest.getStateAction() != null &&
                updateEventAdminRequest.getStateAction().equals(UpdateEventAdminRequest.State.REJECT_EVENT)
                && event.getState().equals(Event.State.PUBLISHED)) {
            throw new ConditionsException("Only not published event can be rejected, but it is " + event.getState());
        }

        Category category = null;
        if (updateEventAdminRequest.getCategory() != null) {
            Optional<Category> categoryContainer = categoryRepository.findById(updateEventAdminRequest.getCategory());
            category = categoryContainer.orElseThrow(() ->
                    new ValidationException("Category with id=" + updateEventAdminRequest.getCategory() + " doesn't exists"));
        }

        Location location = null;
        if (updateEventAdminRequest.getLocation() != null) {
            location = locationService.getOrCreate(updateEventAdminRequest.getLocation());
        }

        eventMapper.updateEvent(event, updateEventAdminRequest, category, location, publishedOn);
        event = eventRepository.save(event);

        log.info("Event with id={} was updated with: {}", eventId, updateEventAdminRequest);

        long eventViewsNumbers = getEventViewsNumber(eventId);
        return eventMapper.toEventFullDto(event, eventViewsNumbers);
    }

    @Override
    public List<EventFullDto> getEvents(List<Long> usersIds,
                                 List<Event.State> eventsStates,
                                 List<Long> categoriesIds,
                                 LocalDateTime rangeStart,
                                 LocalDateTime rangeEnd,
                                 int from,
                                 int size) {

        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));
        List<Event> events = eventRepository.findAllWithFilters(usersIds, eventsStates, categoriesIds,
                rangeStart, rangeEnd, pageRequest);

        List<EventFullDto> dtos = eventMapper.toEventFullDto(events);
        loadFullEventsViewsNumber(dtos);

        log.info("Get events response for users={}, states={}, categories={}, start={}, end={}, from={}, size={}. " +
                "Returned: {}",
                usersIds, eventsStates, categoriesIds, rangeStart, rangeEnd, from, size,
                events.stream().map(Event::getId).collect(Collectors.toList()));

        return dtos;
    }

    @Override
    @Transactional
    public EventFullDto addEvent(long userId, NewEventDto newEventDto) {
        Optional<User> userContainer = userRepository.findById(userId);
        if (userContainer.isEmpty()) {
            throw new EntityNotFoundException("User with id=" + userId + " not found.");
        }
        User initiator = userContainer.get();

        Optional<Category> categoryContainer = categoryRepository.findById(newEventDto.getCategory());
        if (categoryContainer.isEmpty()) {
            throw new EntityNotFoundException("Category with id=" + newEventDto.getCategory() + " not found.");
        }
        Category category = categoryContainer.get();

        Location location = locationService.getOrCreate(newEventDto.getLocation());

        Event event = eventRepository.save(eventMapper.toEvent(newEventDto, category, initiator, location));
        log.info("Event added: {}", event);
        return eventMapper.toEventFullDto(event, 0L);
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User with id=" + userId + " not found.");
        }

        Optional<Event> eventContainer = eventRepository.findById(eventId);
        if (eventContainer.isEmpty()) {
            throw new EntityNotFoundException("Event with id=" + eventId + " not found.");
        }
        Event event = eventContainer.get();

        if (event.getInitiator().getId() != userId) {
            throw new EntityNotAccessibleException("Event with id=" + eventId + " is not owned by user with id=" + userId);
        }

        if (event.getState().equals(Event.State.PUBLISHED)) {
            throw new ConditionsException("Only pending or canceled events can be changed.");
        }

        Category category = null;
        if (updateEventUserRequest.getCategory() != null) {
            Optional<Category> categoryContainer = categoryRepository.findById(updateEventUserRequest.getCategory());
            category = categoryContainer.orElseThrow(() ->
                    new ValidationException("Category with id=" + updateEventUserRequest.getCategory() + " doesn't exists"));
        }

        Location location = null;
        if (updateEventUserRequest.getLocation() != null) {
            location = locationService.getOrCreate(updateEventUserRequest.getLocation());
        }

        eventMapper.updateEvent(event, updateEventUserRequest, category, location);
        event = eventRepository.save(event);

        log.info("Event with id={} was updated with: {}", eventId, updateEventUserRequest);

        long eventViewsNumbers = getEventViewsNumber(eventId);
        return eventMapper.toEventFullDto(event, eventViewsNumbers);
    }

    @Override
    public EventFullDto getEvent(long userId, long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User with id=" + userId + " not found.");
        }

        Optional<Event> eventContainer = eventRepository.findById(eventId);
        if (eventContainer.isEmpty()) {
            throw new EntityNotFoundException("Event with id=" + eventId + " not found.");
        }
        Event event = eventContainer.get();

        if (event.getInitiator().getId() != userId) {
            throw new EntityNotAccessibleException("Event with id=" + eventId + " is not owned by user with id=" + userId);
        }

        long eventViewsNumbers = getEventViewsNumber(eventId);

        log.info("Get event response for userId={}, eventId={} was send.", userId, eventId);

        return eventMapper.toEventFullDto(event, eventViewsNumbers);
    }

    @Override
    public List<EventShortDto> getEvents(long userId, int from, int size) {
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageRequest);

        List<EventShortDto> dtos = eventMapper.toEventShortDto(events);
        loadShortEventsViewsNumber(dtos);

        log.info("Get events response for userId={}, from={}, size={} was send. " +
                "Returned: {}",
                userId, from, size,
                events.stream().map(Event::getId).collect(Collectors.toList()));

        return dtos;
    }

    @Override
    public EventFullDto getEvent(long eventId, HttpServletRequest request) {
        Optional<Event> eventContainer = eventRepository.findById(eventId);
        if (eventContainer.isEmpty()) {
            throw new EntityNotFoundException("Event with id=" + eventId + " not found.");
        }

        Event event = eventContainer.get();
        if (!event.getState().equals(Event.State.PUBLISHED)) {
            throw new EntityNotFoundException("Only published events available for view. But is " + event.getState());
        }

        long eventViewsNumbers = getEventViewsNumber(eventId);
        sendStats(request);

        log.info("Get event response for eventId={} was send.", eventId);

        return eventMapper.toEventFullDto(event, eventViewsNumbers);
    }

    @Override
    public List<EventShortDto> getEvents(String text,
                                         List<Long> categoriesIds,
                                         Boolean paid,
                                         LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd,
                                         Boolean onlyAvailable,
                                         Event.SearchSort sort,
                                         int from,
                                         int size,
                                         HttpServletRequest request) {

        if (categoriesIds != null && !categoryRepository.existsAllByIdIn(categoriesIds)) {
            throw new ValidationException("Not all categories exists.");
        }

        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "eventDate"));
        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
        }

        List<Event> events = eventRepository.findAllWithFilters(Event.State.PUBLISHED,
                                                                text,
                                                                categoriesIds,
                                                                paid,
                                                                rangeStart,
                                                                rangeEnd,
                                                                onlyAvailable,
                                                                pageRequest);

        List<EventShortDto> dtos = eventMapper.toEventShortDto(events);
        loadShortEventsViewsNumber(dtos);

        if (sort != null && sort.equals(Event.SearchSort.VIEWS)) {
            dtos.sort(Comparator.comparing(dto -> dto.getViews()));
        }

        sendStats(request);

        log.info("Get events response for text={}, categories={}, paid={}, start={}, end={}, onlyAvailable={}, sort={}, from={}, size={}. " +
                "Returned: {}.",
                text, categoriesIds, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size,
                dtos.stream().map(EventShortDto::getId).collect(Collectors.toList()));

        return dtos;
    }

    @Override
    public Long getEventViewsNumber(Long eventId) {
        Map<Long, Long> eventViews = getViewForEvents(List.of(eventId));
        return eventViews.get(eventId);
    }

    @Override
    public List<EventShortDto> loadShortEventsViewsNumber(List<EventShortDto> dtos) {
        Map<Long, Long> eventsViews = getViewForEvents(dtos.stream().map(EventShortDto::getId).collect(Collectors.toList()));
        for (EventShortDto dto : dtos) {
            dto.setViews(eventsViews.get(dto.getId()));
        }
        return dtos;
    }

    @Override
    public List<EventFullDto> loadFullEventsViewsNumber(List<EventFullDto> dtos) {
        Map<Long, Long> eventsViews = getViewForEvents(dtos.stream().map(EventFullDto::getId).collect(Collectors.toList()));
        for (EventFullDto dto : dtos) {
            dto.setViews(eventsViews.get(dto.getId()));
        }
        return dtos;
    }

    private void sendStats(HttpServletRequest request) {
        log.info("Send hit for {}", request.getRequestURI());
        statsClient.saveHitSync(environment.getProperty("application.name"),
                                 request.getRequestURI(),
                                 request.getRemoteAddr(),
                                 LocalDateTime.now());
    }

    private Map<Long, Long> getViewForEvents(List<Long> eventsIds) {
        List<String> uris = eventsIds.stream().map(id -> "/events/" + id).collect(Collectors.toList());

        log.info("Send get views request for events ids: {}.", eventsIds);
        List<ViewStats> viewsStats = statsClient.getStats(null, null, uris, true);

        Map<Long, Long> eventsViews = viewsStats.stream()
                .collect(Collectors.toMap(
                        viewStats -> Long.parseLong(viewStats.getUri().substring(viewStats.getUri().lastIndexOf("/") + 1)),
                        ViewStats::getHits));

        for (Long eventId : eventsIds) {
            if (!eventsViews.containsKey(eventId)) {
                eventsViews.put(eventId, 0L);
            }
        }

        return eventsViews;
    }

}
