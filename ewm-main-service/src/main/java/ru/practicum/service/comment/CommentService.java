package ru.practicum.service.comment;

import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.CommentReportDto;
import ru.practicum.dto.comment.NewCommentDto;

import java.util.List;

public interface CommentService {
    CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    CommentDto updateComment(Long userId, Long eventId, Long commentId, NewCommentDto updateCommentDto);

    void deleteComment(Long userId, Long eventId, Long commentId);

    void reportComment(Long userId, Long eventId, Long commentId, CommentReportDto reportDto);

    CommentDto getCommentById(Long eventId, Long commentId);

    List<CommentDto> getEventComments(Long eventId, Integer from, Integer size);

    List<CommentDto> getUserComments(Long userId, Long eventId, Integer from, Integer size);

    List<CommentDto> getReportedComments(Integer from, Integer size);

    List<CommentDto> getDeletedComments(Integer from, Integer size);

    void deleteCommentByAdmin(Long eventId, Long commentId);

    CommentDto restoreComment(Long eventId, Long commentId);
}