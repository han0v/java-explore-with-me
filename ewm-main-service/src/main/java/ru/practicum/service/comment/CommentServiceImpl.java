package ru.practicum.service.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.CommentReportDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.*;
import ru.practicum.repository.comment.CommentReportRepository;
import ru.practicum.repository.comment.CommentRepository;
import ru.practicum.repository.event.EventRepository;
import ru.practicum.repository.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private static final int REPORT_THRESHOLD = 10;

    private final CommentRepository commentRepository;
    private final CommentReportRepository reportRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        Event event = getEventOrThrow(eventId);
        User user = getUserOrThrow(userId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot comment on unpublished event");
        }

        Comment comment = commentMapper.toComment(newCommentDto);
        comment.setEvent(event);
        comment.setUser(user);
        comment.setReportCount(0);
        comment.setIsEdited(false);
        comment.setIsDeleted(false);

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toCommentDto(savedComment);
    }


    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long eventId, Long commentId, NewCommentDto updateCommentDto) {
        Comment comment = getCommentOrThrow(commentId, eventId, userId);

        if (comment.getIsDeleted()) {
            throw new ConflictException("Cannot update deleted comment");
        }

        commentMapper.updateCommentFromDto(updateCommentDto, comment);
        comment.setUpdatedAt(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);
        return commentMapper.toCommentDto(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long eventId, Long commentId) {
        Comment comment = getCommentOrThrow(commentId, eventId, userId);
        comment.setIsDeleted(true);
        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void reportComment(Long userId, Long eventId, Long commentId, CommentReportDto reportDto) {
        Comment comment = getCommentOrThrow(commentId, eventId);
        User user = getUserOrThrow(userId);

        if (comment.getUser().getId().equals(userId)) {
            throw new ConflictException("Cannot report your own comment");
        }

        if (reportRepository.existsByCommentIdAndUserId(commentId, userId)) {
            throw new ConflictException("You have already reported this comment");
        }

        CommentReport report = CommentReport.builder()
                .comment(comment)
                .user(user)
                .reportedAt(LocalDateTime.now())
                .reason(reportDto.getReason())
                .build();

        reportRepository.save(report);

        comment.setReportCount(comment.getReportCount() + 1);
        if (comment.getReportCount() >= REPORT_THRESHOLD) {
            comment.setIsDeleted(true);
        }
        commentRepository.save(comment);
    }

    @Override
    public CommentDto getCommentById(Long eventId, Long commentId) {
        Comment comment = getCommentOrThrow(commentId, eventId);
        if (comment.getIsDeleted()) {
            throw new NotFoundException("Comment not found");
        }
        return commentMapper.toCommentDto(comment);
    }

    @Override
    public List<CommentDto> getEventComments(Long eventId, Integer from, Integer size) {
        getEventOrThrow(eventId);
        PageRequest pageRequest = PageRequest.of(from / size, size);
        return commentRepository.findAllByEventIdAndIsDeleted(eventId, false, pageRequest)
                .stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getUserComments(Long userId, Long eventId, Integer from, Integer size) {
        getUserOrThrow(userId);
        getEventOrThrow(eventId);
        PageRequest pageRequest = PageRequest.of(from / size, size);
        return commentRepository.findAllByUserIdAndEventId(userId, eventId, pageRequest)
                .stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getReportedComments(Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        return commentRepository.findAllReportedComments(pageRequest)
                .stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getDeletedComments(Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        return commentRepository.findAllDeletedComments(pageRequest)
                .stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long eventId, Long commentId) {
        Comment comment = getCommentOrThrow(commentId, eventId);
        comment.setIsDeleted(true);
        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public CommentDto restoreComment(Long eventId, Long commentId) {
        Comment comment = getCommentOrThrow(commentId, eventId);
        comment.setIsDeleted(false);
        Comment restoredComment = commentRepository.save(comment);
        return commentMapper.toCommentDto(restoredComment);
    }

    private Comment getCommentOrThrow(Long commentId, Long eventId) {
        return commentRepository.findByIdAndEventId(commentId, eventId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
    }

    private Comment getCommentOrThrow(Long commentId, Long eventId, Long userId) {
        return commentRepository.findByIdAndUserIdAndEventId(commentId, userId, eventId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
    }

    private Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
