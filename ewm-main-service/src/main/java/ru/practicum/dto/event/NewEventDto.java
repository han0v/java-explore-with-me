package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;
import ru.practicum.model.Location;

import java.time.LocalDateTime;

@Data
@Builder(builderClassName = "NewEventDtoBuilder")
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class NewEventDto {
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    @NotBlank(message = "Annotation cannot be blank")
    @Size(min = 20, max = 2000, message = "Annotation must be between 20 and 2000 characters")
    private String annotation;

    @NotNull(message = "Category ID cannot be null")
    @Positive(message = "Category ID must be positive")
    private Long category;

    @NotBlank(message = "Description cannot be blank")
    @Size(min = 20, max = 7000, message = "Description must be between 20 and 7000 characters")
    private String description;

    @NotNull(message = "Event date cannot be null")
    @Future(message = "Event date must be in the future")
    @JsonFormat(pattern = DATE_TIME_FORMAT)
    private LocalDateTime eventDate;

    @NotNull(message = "Location cannot be null")
    @Valid
    private Location location;

    @Builder.Default
    private Boolean paid = false;

    @Builder.Default
    @Min(value = 0, message = "Participant limit cannot be negative")
    private Integer participantLimit = 0;

    @Builder.Default
    private Boolean requestModeration = true;

    @NotBlank(message = "Title cannot be blank")
    @Size(min = 3, max = 120, message = "Title must be between 3 and 120 characters")
    private String title;

}