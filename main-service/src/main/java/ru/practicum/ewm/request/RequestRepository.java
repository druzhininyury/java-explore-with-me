package ru.practicum.ewm.request;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.request.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    boolean existsByRequesterIdAndEventId(long requesterId, long eventId);

    List<Request> findAllByRequesterId(long requesterId);

    List<Request> findAllByIdIn(List<Long> requestsIds);

    List<Request> findAllByEventId(long eventId);
}
