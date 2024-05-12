package ru.practicum.ewm.like.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeDto {

    private Long id;

    private Long eventId;

    private Boolean isPositive;

    private Long userId;

}
