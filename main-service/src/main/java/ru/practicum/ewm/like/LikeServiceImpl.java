package ru.practicum.ewm.like;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConditionsException;
import ru.practicum.ewm.exception.EntityNotFoundException;
import ru.practicum.ewm.like.dto.LikeDto;
import ru.practicum.ewm.like.mapping.LikeMapper;
import ru.practicum.ewm.like.model.Like;
import ru.practicum.ewm.user.UserRepository;
import ru.practicum.ewm.user.model.User;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService{

    private final LikeMapper likeMapper;

    private final LikeRepository likeRepository;

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public LikeDto addLike(long userId, long eventId, boolean positive) {
        Optional<User> userContainer = userRepository.findById(userId);
        if (userContainer.isEmpty()) {
            throw new EntityNotFoundException("User with id=" + userId + " not found.");
        }
        User user = userContainer.get();

        Optional<Event> eventContainer = eventRepository.findById(eventId);
        if (eventContainer.isEmpty()) {
            throw new EntityNotFoundException("Event with id=" + eventId + " not found.");
        }
        Event event = eventContainer.get();

        if (likeRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw new ConditionsException("User(id=" + userId + ") already liked event(id=" + eventId + ")");
        }

        if (!event.getState().equals(Event.State.PUBLISHED)) {
            throw new ConditionsException("Can't like event(id" + eventId + ") because it is not published.");
        }

        if (event.getInitiator().getId() == userId) {
            throw new ConditionsException("Event(id=" + eventId + ") initiator(id=" + userId + ") can't like own events.");
        }

        Like like = Like.builder()
                .event(event)
                .positive(positive)
                .user(user)
                .build();

        like = likeRepository.save(like);
        eventRepository.incrementLikesAndDislikes(eventId, positive ? 1 : 0, positive ? 0 : 1);

        log.info("Like added: " + like);

        return likeMapper.toLikeDto(like);
    }

    @Override
    @Transactional
    public LikeDto updateLike(long userId, long eventId, boolean positive) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User with id=" + userId + " not found.");
        }

        if (!eventRepository.existsById(eventId)) {
            throw new EntityNotFoundException("Event with id=" + eventId + " not found.");
        }

        Optional<Like> likeContainer = likeRepository.findByEventIdAndUserId(eventId, userId);
        if (likeContainer.isEmpty()) {
            throw new EntityNotFoundException("Like for event(id=" + eventId + ") by user(id=" + userId + ") not found.");
        }
        Like like = likeContainer.get();

        if (like.isPositive() != positive) {
            like.setPositive(positive);
            like = likeRepository.save(like);
            eventRepository.incrementLikesAndDislikes(eventId, positive ? 1 : -1, positive ? -1 : 1);
        }

        log.info("Like updated: " + like);

        return likeMapper.toLikeDto(like);
    }

    @Override
    @Transactional
    public void deleteLike(long userId, long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User with id=" + userId + " not found.");
        }

        if (!eventRepository.existsById(eventId)) {
            throw new EntityNotFoundException("Event with id=" + eventId + " not found.");
        }

        Optional<Like> likeContainer = likeRepository.findByEventIdAndUserId(eventId, userId);
        if (likeContainer.isEmpty()) {
            throw new EntityNotFoundException("Like for event(id=" + eventId + ") by user(id=" + userId + ") not found.");
        }
        Like like = likeContainer.get();

        likeRepository.deleteById(like.getId());
        eventRepository.incrementLikesAndDislikes(eventId, like.isPositive() ? -1 : 0, like.isPositive() ? 0 : -1);

        log.info("Like deleted: " + like);
    }

}
