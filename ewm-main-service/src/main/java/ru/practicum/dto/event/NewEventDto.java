package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.validation.annotation.Validated;
import ru.practicum.model.Location;

import java.time.LocalDateTime;

@Data
@Builder(builderClassName = "NewEventDtoBuilder")
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class NewEventDto {
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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

    // Кастомный Builder с ручной валидацией
    public static class NewEventDtoBuilder {
        private String annotation;
        private String description;
        private String title;
        private Integer participantLimit = 0;

        public NewEventDtoBuilder annotation(String annotation) {
            if (annotation == null || annotation.trim().isEmpty()) {
                throw new IllegalArgumentException("Annotation cannot be blank or contain only spaces");
            }
            if (annotation.length() < 20 || annotation.length() > 2000) {
                throw new IllegalArgumentException("Annotation must be between 20 and 2000 characters");
            }
            this.annotation = annotation;
            return this;
        }

        public NewEventDtoBuilder description(String description) {
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalArgumentException("Description cannot be blank or contain only spaces");
            }
            if (description.length() < 20 || description.length() > 7000) {
                throw new IllegalArgumentException("Description must be between 20 and 7000 characters");
            }
            this.description = description;
            return this;
        }

        public NewEventDtoBuilder title(String title) {
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Title cannot be blank or contain only spaces");
            }
            if (title.length() < 3 || title.length() > 120) {
                throw new IllegalArgumentException("Title must be between 3 and 120 characters");
            }
            this.title = title;
            return this;
        }

        public NewEventDtoBuilder participantLimit(Integer participantLimit) {
            if (participantLimit != null && participantLimit < 0) {
                throw new IllegalArgumentException("Participant limit cannot be negative");
            }
            this.participantLimit = participantLimit != null ? participantLimit : 0;
            return this;
        }
    }
}