package ru.practicum.ewm.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCategoryDto {

    @Size(min = CategoryDto.NAME_MIN_LENGTH, max = CategoryDto.NAME_MAX_LENGTH, message = CategoryDto.NAME_SIZE_ERROR_MESSAGE)
    private String name;

}
