package ru.practicum.repository.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Comment;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findAllByEventIdAndIsDeleted(Long eventId, Boolean isDeleted, Pageable pageable);

    Page<Comment> findAllByUserIdAndEventId(Long userId, Long eventId, Pageable pageable);

    Optional<Comment> findByIdAndEventId(Long commentId, Long eventId);

    Optional<Comment> findByIdAndUserIdAndEventId(Long commentId, Long userId, Long eventId);

    @Query("SELECT c FROM Comment c WHERE c.reportCount > 0 AND c.isDeleted = false")
    Page<Comment> findAllReportedComments(Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.isDeleted = true")
    Page<Comment> findAllDeletedComments(Pageable pageable);
}