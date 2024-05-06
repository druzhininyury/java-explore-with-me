package ru.practicum.ewm.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    public static final String EMAIL_NULL_ERROR_MESSAGE = "User email must be provided.";
    public static final String EMAIL_CORRECTNESS_ERROR_MESSAGE = "User e-mail is incorrect.";
    public static final int EMAIL_MIN_LENGTH = 6;
    public static final int EMAIL_MAX_LENGTH = 254;
    public static final String EMAIL_LENGTH_ERROR_MESSAGE = "User e-mail length must be between "
        + EMAIL_MIN_LENGTH + " and " + EMAIL_MAX_LENGTH + ".";

    public static final String NAME_BLANK_ERROR_MESSAGE = "User name can't be blank.";
    public static final int NAME_MIN_LENGTH = 2;
    public static final int NAME_MAX_LENGTH = 250;
    public static final String NAME_LENGTH_ERROR_MESSAGE = "User name length must be between "
            + NAME_MIN_LENGTH + " and " + NAME_MAX_LENGTH + ".";

    private long id;

    @NotNull(message = EMAIL_NULL_ERROR_MESSAGE)
    @Email(message = EMAIL_CORRECTNESS_ERROR_MESSAGE)
    @Size(min = EMAIL_MIN_LENGTH, max = EMAIL_MAX_LENGTH, message = EMAIL_LENGTH_ERROR_MESSAGE)
    private String email;

    @NotBlank(message = NAME_BLANK_ERROR_MESSAGE)
    @Size(min = NAME_MIN_LENGTH, max = NAME_MAX_LENGTH, message = NAME_LENGTH_ERROR_MESSAGE)
    private String name;

}
