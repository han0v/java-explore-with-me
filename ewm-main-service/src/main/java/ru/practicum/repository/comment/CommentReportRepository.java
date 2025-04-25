package ru.practicum.repository.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.CommentReport;
import ru.practicum.model.CommentReportId;

public interface CommentReportRepository extends JpaRepository<CommentReport, CommentReportId> {
    boolean existsByCommentIdAndUserId(Long commentId, Long userId);
}