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

    public enum LikesLevel {
        OVERWHELMINGLY_POSITIVE("Overwhelmingly positive"),
        VERY_POSITIVE("Very positive"),
        POSITIVE("Positive"),
        MOSTLY_POSITIVE("Mostly positive"),
        MIXED("Mixed"),
        MOSTLY_NEGATIVE("Mostly negative"),
        NEGATIVE("Negative"),
        VERY_NEGATIVE("Very negative"),
        OVERWHELMINGLY_NEGATIVE("Overwhelmingly negative");

        private final String outputName;

        LikesLevel(String outputName) {
            this.outputName = outputName;
        }

        public static LikesLevel getLevel(int percent) {
            if (percent < 19) {
                return OVERWHELMINGLY_NEGATIVE;
            } else if (percent < 29) {
                return VERY_NEGATIVE;
            } else if (percent < 39) {
                return NEGATIVE;
            } else if (percent < 49) {
                return MOSTLY_NEGATIVE;
            } else if (percent < 59) {
                return MIXED;
            } else if (percent < 69) {
                return MOSTLY_POSITIVE;
            } else if (percent < 79) {
                return POSITIVE;
            } else if (percent < 89) {
                return VERY_POSITIVE;
            } else {
                return OVERWHELMINGLY_POSITIVE;
            }
        }

        @Override
        public String toString() {
            return outputName;
        }

    }

    private Long id;

    private Long eventId;

    private Boolean isPositive;

    private Long userId;

}
