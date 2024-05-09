package ru.practicum.ewm.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConditionsException;
import ru.practicum.ewm.exception.EntityNotFoundException;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapping.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.user.UserRepository;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService{

    private final RequestMapper requestMapper;

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public ParticipationRequestDto addRequest(long userId, long eventId) {
        Optional<User> userContainer = userRepository.findById(userId);
        if (userContainer.isEmpty()) {
            throw new EntityNotFoundException("User with id=" + userId + " not found.");
        }
        User requester = userContainer.get();

        Optional<Event> eventContainer = eventRepository.findById(eventId);
        if (eventContainer.isEmpty()) {
            throw new EntityNotFoundException("Event with id=" + eventId + " not found.");
        }
        Event event = eventContainer.get();

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConditionsException("Request with requester id=" + userId + " and event id=" + eventId + " already exists");
        }

        if (userId == event.getInitiator().getId()) {
            throw new ConditionsException("User(id=" + userId + ") can't make request for his own event(id=" + eventId + ").");
        }

        if (!event.getState().equals(Event.State.PUBLISHED)) {
            throw new ConditionsException("User(id=" + userId + ") can't make request for not published event(id=" + eventId + ").");
        }

        if (event.getParticipantLimit() > 0 && event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConditionsException("User(id=" + userId + ") can't make request for event(id=" + eventId + ") with no room.");
        }

        Request request = Request.builder()
                .requester(requester)
                .event(event)
                .createdOn(LocalDateTime.now())
                .state(event.getParticipantLimit() > 0 && event.isRequestModeration() ? Request.State.PENDING : Request.State.CONFIRMED)
                .build();

        request = requestRepository.save(request);
        if (request.getState().equals(Request.State.CONFIRMED)) {
            incrementConfirmedRequestsInEvent(eventId, 1);
        }

        log.info("Request added: " + request);

        return requestMapper.toParticipationRequestDto(request);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(long userId, long requestId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User with id=" + userId + " not found.");
        }

        Optional<Request> requestContainer = requestRepository.findById(requestId);
        if (requestContainer.isEmpty()) {
            throw new EntityNotFoundException("Request with id=" + requestId + " not found");
        }
        Request request = requestContainer.get();

        if (userId != request.getRequester().getId()) {
            throw new ConditionsException("User(id=" + userId + ") can't cancel not his own request(id=" + requestId + ").");
        }

        if (request.getState().equals(Request.State.CANCELED)) {
            throw new ConditionsException("Can't cancel already canceled request(id=" + requestId +").");
        }

        if (request.getState().equals(Request.State.REJECTED)) {
            throw new ConditionsException("Can't cancel already rejected request(id=" + requestId +").");
        }

        if (request.getState().equals(Request.State.CONFIRMED)) {
            incrementConfirmedRequestsInEvent(request.getEvent().getId(), -1);
        }
        request.setState(Request.State.CANCELED);
        request = requestRepository.save(request);

        log.info("Request canceled: {}", request);

        return requestMapper.toParticipationRequestDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getRequests(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User with id=" + userId + " not found.");
        }

        List<Request> requests = requestRepository.findAllByRequesterId(userId);

        log.info("Response for get requests for requester(id={}) was send.", userId);

        return requestMapper.toParticipationRequestDto(requests);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestsStatus(
            long userId,
            long eventId,
            EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {

        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User with id=" + userId + " not found.");
        }

        Optional<Event> eventContainer = eventRepository.findById(eventId);
        if (eventContainer.isEmpty()) {
            throw new EntityNotFoundException("Event with id=" + eventId + " not found.");
        }
        Event event = eventContainer.get();

        if (userId != event.getInitiator().getId()) {
            throw new ConditionsException("User(id=" + userId + ") is not initiator of of event(id=" + eventId +").");
        }

        if (eventRequestStatusUpdateRequest.getStatus().equals(EventRequestStatusUpdateRequest.State.CONFIRMED) &&
                event.getParticipantLimit() > 0 &&
                event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConditionsException("Can't confirm requests because participant limit reached in event(id=" +
                    eventId + ").");
        }

        List<Request> requests = requestRepository.findAllByIdIn(eventRequestStatusUpdateRequest.getRequestIds());
        List<Long> badRequest = null;

        badRequest = requests.stream().filter(request -> request.getEvent().getId() != eventId)
                .map(Request::getId).collect(Collectors.toList());
        if (!badRequest.isEmpty()) {
            throw new ConditionsException("Can't update requests statuses, some request are not for event(id=" +
                    eventId +"). Invalid requests ids: " + badRequest);
        }

        badRequest = requests.stream().filter(request -> !request.getState().equals(Request.State.PENDING))
                .map(Request::getId).collect(Collectors.toList());
        if (!badRequest.isEmpty()) {
            throw new ConditionsException("Can't update requests statuses, some request are not pending. " +
                    "Invalid requests ids: " + badRequest);
        }

        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();
        int requestsToConfirm =
                eventRequestStatusUpdateRequest.getStatus().equals(EventRequestStatusUpdateRequest.State.CONFIRMED) ?
                        requests.size() : 0;

        for (Request request : requests) {
            if (event.getParticipantLimit() > 0 &&
                    requestsToConfirm > 0 &&
                    event.getConfirmedRequests() + confirmedRequests.size() <  event.getParticipantLimit()) {
                request.setState(Request.State.CONFIRMED);
                confirmedRequests.add(request);
            } else {
                request.setState(Request.State.REJECTED);
                rejectedRequests.add(request);
            }
        }

        requestRepository.saveAll(requests);
        if (!confirmedRequests.isEmpty()) {
            incrementConfirmedRequestsInEvent(eventId, confirmedRequests.size());
        }

        EventRequestStatusUpdateResult result = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(requestMapper.toParticipationRequestDto(confirmedRequests))
                .rejectedRequests(requestMapper.toParticipationRequestDto(rejectedRequests))
                .build();

        log.info("Request statuses updated by event(id={}) initiator(id={}). " +
                "Requests confirmed={} and rejected={}.", eventId, userId,
                confirmedRequests.stream().map(Request::getId).collect(Collectors.toList()),
                rejectedRequests.stream().map(Request::getId).collect(Collectors.toList()));

        return result;
    }

    @Override
    public List<ParticipationRequestDto> getRequests(long userId, long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User with id=" + userId + " not found.");
        }

        Optional<Event> eventContainer = eventRepository.findById(eventId);
        if (eventContainer.isEmpty()) {
            throw new EntityNotFoundException("Event with id=" + eventId + " not found.");
        }
        Event event = eventContainer.get();

        if (event.getInitiator().getId() != userId) {
            throw new ConditionsException("User(id=" + userId + ") is not initiator of event(id=" + eventId + ").");
        }

        List<Request> requests = requestRepository.findAllByEventId(eventId);

        log.info("Response for get request of user(id={}) and his event(id={}) was send.", userId, eventId);

        return requestMapper.toParticipationRequestDto(requests);
    }

    @Transactional
    private void incrementConfirmedRequestsInEvent(long eventId, long amount) {
        eventRepository.incrementConfirmedRequests(eventId, amount);

        log.info("Event with id={} confirmed requests incremented by {}", eventId, amount);
    }

}
