package ru.practicum.ewm.like;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.like.model.Like;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByEventIdAndUserId(long eventId, long userId);

    Optional<Like> findByEventIdAndUserId(long eventId, long userId);

}
