package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Future;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

import static ru.practicum.ewm.event.dto.NewEventDto.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest {

    public static final String EVENT_DATE_IN_PAST_ERROR_MESSAGE = "New event date can't be in past.";

    public enum State {
        PUBLISH_EVENT,
        REJECT_EVENT
    }

    @Size(min = ANNOTATION_MIN_LENGTH, max = ANNOTATION_MAX_LENGTH, message = ANNOTATION_LENGTH_ERROR_MESSAGE)
    private String annotation;

    private Long category;

    @Size(min = DESCRIPTION_MIN_LENGTH, max = DESCRIPTION_MAX_LENGTH, message = DESCRIPTION_LENGTH_ERROR_MESSAGE)
    private String description;

    @Future(message = EVENT_DATE_IN_PAST_ERROR_MESSAGE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = EventFullDto.DATE_TIME_FORMAT)
    private LocalDateTime eventDate;

    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero(message = PARTICIPANT_LIMIT_NEGATIVE_ERROR_MESSAGE)
    private Integer participantLimit;

    private Boolean requestModeration;

    private State stateAction;

    @Size(min = TITLE_MIN_LENGTH, max = TITLE_MAX_LENGTH, message = TITLE_LENGTH_ERROR_MESSAGE)
    private String title;

}
