package ru.practicum.ewm.event.model;

import lombok.*;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Event {

    public enum State {
        PENDING,
        PUBLISHED,
        CANCELED
    }

    public enum SearchSort {
        EVENT_DATE,
        VIEWS
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "annotation", nullable = false)
    private String annotation;

    @JoinColumn(name = "category_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    @Column(name = "confirmed_requests")
    private long confirmedRequests;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @JoinColumn(name = "initiator_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User initiator;

    @JoinColumn(name = "location_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Location location;

    @Column(name = "paid")
    private boolean paid;

    @Column(name = "participant_limit")
    private long participantLimit;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Column(name = "request_moderation")
    private boolean requestModeration;

    @Builder.Default
    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private State state = State.PENDING;

    @Column(name = "title", nullable = false)
    private String title;

}
