package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByInitiatorId(long initiatorId, Pageable pageable);

    @Query("select event " +
            "from Event event " +
            "where ((:usersIds) is null or event.initiator.id in (:usersIds)) " +
            "and ((:eventsStates) is null or event.state in (:eventsStates)) " +
            "and ((:categoriesIds) is null or event.category.id in (:categoriesIds)) " +
            "and (cast(:rangeStart as timestamp) is null or event.eventDate >= :rangeStart) " +
            "and (cast(:rangeEnd as timestamp) is null or event.eventDate <= :rangeEnd) ")
    List<Event> findAllWithFilters(@Param("usersIds") List<Long> usersIds,
                                   @Param("eventsStates") List<Event.State> eventsStates,
                                   @Param("categoriesIds") List<Long> categoriesIds,
                                   @Param("rangeStart") LocalDateTime rangeStart,
                                   @Param("rangeEnd") LocalDateTime rangeEnd,
                                   Pageable pageable);

    @Query("select event " +
           "from Event event " +
           "where (:state is null or event.state = :state) " +
           "and (:text is null or lower(event.annotation) like lower(concat('%', :text, '%')) " + " " +
           " or lower(event.description) like lower(concat('%', :text, '%'))) " +
           "and ((:categoriesIds) is null or event.category.id in (:categoriesIds)) " +
           "and (:paid is null or event.paid = :paid) " +
           "and (cast(:rangeStart as timestamp) is null or event.eventDate >= :rangeStart) " +
           "and (cast(:rangeEnd as timestamp) is null or event.eventDate <= :rangeEnd) " +
           "and (:onlyAvailable = false or event.participantLimit = 0 or event.confirmedRequests < event.participantLimit) ")
    List<Event> findAllWithFilters(@Param("state") Event.State state,
                                   @Param("text") String text,
                                   @Param("categoriesIds") List<Long> categoriesIds,
                                   @Param("paid") Boolean paid,
                                   @Param("rangeStart") LocalDateTime rangeStart,
                                   @Param("rangeEnd") LocalDateTime rangeEnd,
                                   @Param("onlyAvailable") Boolean onlyAvailable,
                                   Pageable pageable);

    @Modifying
    @Query("update Event event " +
            "set event.confirmedRequests = event.confirmedRequests + :amount " +
            "where event.id = :eventId ")
    void incrementConfirmedRequests(long eventId, long amount);

    boolean existsAllByIdIn(List<Long> eventIds);

}
