package ru.practicum.ewm.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {

    public static final String REQUESTS_IDS_NULL_ERROR_MESSAGE = "Requests ids must be provided.";

    public static final String STATUS_NULL_ERROR_MESSAGE = "New status for requests must be provided.";

    public enum State {
        CONFIRMED,
        REJECTED
    }

    @NotNull(message =  REQUESTS_IDS_NULL_ERROR_MESSAGE)
    private List<Long> requestIds;

    @NotNull(message = STATUS_NULL_ERROR_MESSAGE)
    private State status;

}
