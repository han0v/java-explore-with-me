package ru.practicum.dto.participationRequest;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConfirmedRequestCount {
    private Long eventId;
    private Long count;
}
