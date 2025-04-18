package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    @Column(nullable = false)
    private Float lat;

    @Column(nullable = false)
    private Float lon;
}