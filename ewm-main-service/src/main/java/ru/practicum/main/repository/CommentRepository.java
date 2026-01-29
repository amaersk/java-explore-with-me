package ru.practicum.main.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.main.model.Comment;
import ru.practicum.main.model.Comment.CommentStatus;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
	Page<Comment> findByEventIdAndStatus(Long eventId, CommentStatus status, Pageable pageable);

	Page<Comment> findByEventId(Long eventId, Pageable pageable);

	Page<Comment> findByAuthorId(Long authorId, Pageable pageable);
}

