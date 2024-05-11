package ru.practicum.ewm.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UniqueElements;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {

    public static final String EVENTS_NOT_UNIQUE_ERROR_MESSAGE = "Compilation events must be unique.";

    public static final String TITLE_BLANK_ERROR_MESSAGE = "Compilation title must not be blank.";
    public static final int TITLE_MIN_LENGTH = 1;
    public static final int TITLE_MAX_LENGTH = 50;
    public static final String TITLE_LENGTH_ERROR_MESSAGE = "Compilation title length must be between " +
            TITLE_MIN_LENGTH + " and " + TITLE_MAX_LENGTH + ".";

    @UniqueElements(message = EVENTS_NOT_UNIQUE_ERROR_MESSAGE)
    private List<Long> events;

    @Builder.Default
    private Boolean pinned = false;

    @NotBlank(message = TITLE_BLANK_ERROR_MESSAGE)
    @Size(min = TITLE_MIN_LENGTH, max = TITLE_MAX_LENGTH, message = TITLE_LENGTH_ERROR_MESSAGE)
    private String title;

}
