package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.validation.EventDateConstraint;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {

    public static final String ANNOTATION_BLANK_ERROR_MESSAGE = "Annotation must not be blank.";
    public static final int ANNOTATION_MIN_LENGTH = 20;
    public static final int ANNOTATION_MAX_LENGTH = 2000;
    public static final String ANNOTATION_LENGTH_ERROR_MESSAGE = "Annotation length can be between "
            + ANNOTATION_MIN_LENGTH + " and " + ANNOTATION_MAX_LENGTH + ".";

    public static final String CATEGORY_NULL_ERROR_MESSAGE = "Category must be provided.";

    public static final String DESCRIPTION_BLANK_ERROR_MESSAGE = "Description must not be blank.";
    public static final int DESCRIPTION_MIN_LENGTH = 20;
    public static final int DESCRIPTION_MAX_LENGTH = 7000;
    public static final String DESCRIPTION_LENGTH_ERROR_MESSAGE = "Annotation length can be between "
            + DESCRIPTION_MIN_LENGTH + " and " + DESCRIPTION_MAX_LENGTH + ".";

    public static final String EVENT_DATE_NULL_ERROR_MESSAGE = "Event date must be provided.";
    public static final int EVENT_DATE_TIME_RESERVE_HOURS = 2;
    public static final String EVENT_DATE_TOO_EARLY_ERROR_MESSAGE = "Event date must be "
        + EVENT_DATE_TIME_RESERVE_HOURS + " hour(s) later then now.";

    public static final String LOCATION_NULL_ERROR_MESSAGE = "Event location must be provided.";

    public static final String PARTICIPANT_LIMIT_NEGATIVE_ERROR_MESSAGE = "Participant limit must be positive or zero.";

    public static final String TITLE_BLANK_ERROR_MESSAGE = "Title must not be blank.";
    public static final int TITLE_MIN_LENGTH = 3;
    public static final int TITLE_MAX_LENGTH = 120;
    public static final String TITLE_LENGTH_ERROR_MESSAGE = "Annotation length can be between "
            + TITLE_MIN_LENGTH + " and " + TITLE_MAX_LENGTH + ".";

    @NotBlank(message = ANNOTATION_BLANK_ERROR_MESSAGE)
    @Size(min = ANNOTATION_MIN_LENGTH, max = ANNOTATION_MAX_LENGTH, message = ANNOTATION_LENGTH_ERROR_MESSAGE)
    private String annotation;

    @NotNull(message = CATEGORY_NULL_ERROR_MESSAGE)
    private Long category;

    @NotBlank(message = DESCRIPTION_BLANK_ERROR_MESSAGE)
    @Size(min = DESCRIPTION_MIN_LENGTH, max = DESCRIPTION_MAX_LENGTH, message = DESCRIPTION_LENGTH_ERROR_MESSAGE)
    private String description;

    @NotNull(message = EVENT_DATE_NULL_ERROR_MESSAGE)
    @EventDateConstraint(hours = EVENT_DATE_TIME_RESERVE_HOURS, message = EVENT_DATE_TOO_EARLY_ERROR_MESSAGE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = EventFullDto.DATE_TIME_FORMAT)
    private LocalDateTime eventDate;

    @NotNull(message = LOCATION_NULL_ERROR_MESSAGE)
    private LocationDto location;

    private Boolean paid = false;

    @PositiveOrZero(message = PARTICIPANT_LIMIT_NEGATIVE_ERROR_MESSAGE)
    private Integer participantLimit = 0;

    private Boolean requestModeration = true;

    @NotBlank (message = TITLE_BLANK_ERROR_MESSAGE)
    @Size(min = TITLE_MIN_LENGTH, max = TITLE_MAX_LENGTH, message = TITLE_LENGTH_ERROR_MESSAGE)
    private String title;


}
