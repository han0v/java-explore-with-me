package ru.practicum.dto.event;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.model.Location;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private Long id;
    private String title;
    private String annotation;
    private String description;
    private CategoryDto category;
    private UserShortDto initiator;
    @JsonFormat(pattern = DATE_TIME_FORMAT)
    private LocalDateTime eventDate;

    @JsonFormat(pattern = DATE_TIME_FORMAT)
    private LocalDateTime createdOn;

    @JsonFormat(pattern = DATE_TIME_FORMAT)
    private LocalDateTime publishedOn;
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private String state;
    private Long confirmedRequests;
    private Long views;
}
