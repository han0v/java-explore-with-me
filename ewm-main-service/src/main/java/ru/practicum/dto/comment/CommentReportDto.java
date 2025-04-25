package ru.practicum.dto.comment;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class CommentReportDto {
    @Size(max = 1000, message = "Причина жалобы не должна превышать 1000 символов")
    private String reason;
}