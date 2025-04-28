package ru.practicum.controller.comment;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.service.comment.CommentService;

import java.util.List;

@RestController
@RequestMapping("/admin/events/{eventId}/comments")
@RequiredArgsConstructor
@Validated
public class AdminCommentController {
    private final CommentService commentService;

    @GetMapping("/reported")
    public List<CommentDto> getReportedComments(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {
        return commentService.getReportedComments(from, size);
    }

    @GetMapping("/deleted")
    public List<CommentDto> getDeletedComments(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {
        return commentService.getDeletedComments(from, size);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByAdmin(
            @PathVariable Long eventId,
            @PathVariable Long commentId) {
        commentService.deleteCommentByAdmin(eventId, commentId);
    }

    @PatchMapping("/{commentId}/restore")
    public CommentDto restoreComment(
            @PathVariable Long eventId,
            @PathVariable Long commentId) {
        return commentService.restoreComment(eventId, commentId);
    }
}