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
public class NewUserRequest {

    @NotNull(message = UserDto.EMAIL_NULL_ERROR_MESSAGE)
    @Email(message = UserDto.EMAIL_CORRECTNESS_ERROR_MESSAGE)
    @Size(min = UserDto.EMAIL_MIN_LENGTH, max = UserDto.EMAIL_MAX_LENGTH, message = UserDto.NAME_LENGTH_ERROR_MESSAGE)
    private String email;

    @NotBlank(message = UserDto.NAME_BLANK_ERROR_MESSAGE)
    @Size(min = UserDto.NAME_MIN_LENGTH, max = UserDto.NAME_MAX_LENGTH, message = UserDto.NAME_LENGTH_ERROR_MESSAGE)
    private String name;

}
