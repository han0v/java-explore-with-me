package ru.practicum.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private Long eventId;
    private Long userId;
    private String userFullName;
    private String text;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer reportCount;
    private Boolean isEdited;
    private Boolean isDeleted;
}