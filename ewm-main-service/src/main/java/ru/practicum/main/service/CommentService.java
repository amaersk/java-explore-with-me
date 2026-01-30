package ru.practicum.main.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.CommentDto;
import ru.practicum.main.dto.ModerateCommentRequest;
import ru.practicum.main.dto.NewCommentDto;
import ru.practicum.main.dto.UpdateCommentDto;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.mapper.CommentMapper;
import ru.practicum.main.model.Comment;
import ru.practicum.main.model.Comment.CommentStatus;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.Event.EventState;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.CommentRepository;

@Service
@Transactional(readOnly = true)
public class CommentService {
	private final CommentRepository commentRepository;
	private final CommentMapper commentMapper;
	private final UserService userService;
	private final EventService eventService;

	public CommentService(CommentRepository commentRepository, CommentMapper commentMapper, UserService userService, EventService eventService) {
		this.commentRepository = commentRepository;
		this.commentMapper = commentMapper;
		this.userService = userService;
		this.eventService = eventService;
	}

	@Transactional
	public CommentDto addComment(Long userId, Long eventId, NewCommentDto dto) {
		User author = userService.findUserById(userId);
		Event event = eventService.findEventById(eventId);
		if (event.getState() != EventState.PUBLISHED) {
			throw new ConflictException("Event must be published to comment");
		}

		Comment comment = new Comment();
		comment.setAuthor(author);
		comment.setEvent(event);
		comment.setText(dto.getText());
		comment.setStatus(CommentStatus.PENDING);
		comment.setCreatedOn(LocalDateTime.now());

		return commentMapper.toCommentDto(commentRepository.save(comment));
	}

	@Transactional
	public CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto dto) {
		Comment comment = findCommentById(commentId);
		if (!comment.getAuthor().getId().equals(userId)) {
			throw new NotFoundException("Comment with id=" + commentId + " was not found");
		}
		if (comment.getStatus() == CommentStatus.REJECTED) {
			throw new ConflictException("Rejected comment cannot be updated");
		}
		comment.setText(dto.getText());
		comment.setUpdatedOn(LocalDateTime.now());
		// re-moderate after edit
		comment.setStatus(CommentStatus.PENDING);
		comment.setModeratedOn(null);
		return commentMapper.toCommentDto(commentRepository.save(comment));
	}

	@Transactional
	public void deleteComment(Long userId, Long commentId) {
		Comment comment = findCommentById(commentId);
		if (!comment.getAuthor().getId().equals(userId)) {
			throw new NotFoundException("Comment with id=" + commentId + " was not found");
		}
		commentRepository.delete(comment);
	}

	public List<CommentDto> getPublicComments(Long eventId, Integer from, Integer size) {
		if (size <= 0) {
			throw new BadRequestException("Size must be greater than 0");
		}
		if (from < 0) {
			throw new BadRequestException("From must be greater than or equal to 0");
		}
		Pageable pageable = PageRequest.of(from / size, size);
		Page<Comment> page = commentRepository.findByEventIdAndStatus(eventId, CommentStatus.PUBLISHED, pageable);
		return page.getContent().stream().map(commentMapper::toCommentDto).toList();
	}

	public List<CommentDto> getAdminComments(Long eventId, Long authorId, String status, Integer from, Integer size) {
		if (size <= 0) {
			throw new BadRequestException("Size must be greater than 0");
		}
		if (from < 0) {
			throw new BadRequestException("From must be greater than or equal to 0");
		}
		Pageable pageable = PageRequest.of(from / size, size);

		// Minimal filtering without specifications to keep it simple
		Page<Comment> page;
		if (eventId != null) {
			page = commentRepository.findByEventId(eventId, pageable);
		} else if (authorId != null) {
			page = commentRepository.findByAuthorId(authorId, pageable);
		} else {
			page = commentRepository.findAll(pageable);
		}

		List<Comment> filtered = page.getContent();
		if (status != null && !status.isBlank()) {
			CommentStatus st;
			try {
				st = CommentStatus.valueOf(status);
			} catch (IllegalArgumentException ex) {
				throw new BadRequestException("Unknown comment status");
			}
			filtered = filtered.stream().filter(c -> c.getStatus() == st).toList();
		}

		return filtered.stream().map(commentMapper::toCommentDto).toList();
	}

	@Transactional
	public CommentDto moderateComment(Long commentId, ModerateCommentRequest req) {
		Comment comment = findCommentById(commentId);
		String action = req.getAction();
		if ("PUBLISH".equals(action)) {
			comment.setStatus(CommentStatus.PUBLISHED);
		} else if ("REJECT".equals(action)) {
			comment.setStatus(CommentStatus.REJECTED);
		} else {
			throw new BadRequestException("Unknown moderation action");
		}
		comment.setModeratedOn(LocalDateTime.now());
		return commentMapper.toCommentDto(commentRepository.save(comment));
	}

	private Comment findCommentById(Long commentId) {
		return commentRepository.findById(commentId)
				.orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));
	}
}

