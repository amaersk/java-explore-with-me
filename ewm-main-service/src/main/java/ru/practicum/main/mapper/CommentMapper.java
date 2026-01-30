package ru.practicum.main.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.main.dto.CommentDto;
import ru.practicum.main.dto.UserShortDto;
import ru.practicum.main.model.Comment;
import ru.practicum.main.model.User;
import ru.practicum.main.util.DateUtil;

@Component
public class CommentMapper {
	public CommentDto toCommentDto(Comment comment) {
		CommentDto dto = new CommentDto();
		dto.setId(comment.getId());
		dto.setEventId(comment.getEvent().getId());
		dto.setAuthor(toUserShortDto(comment.getAuthor()));
		dto.setText(comment.getText());
		dto.setStatus(comment.getStatus().name());
		dto.setCreatedOn(comment.getCreatedOn().format(DateUtil.MAIN_FORMATTER));
		if (comment.getUpdatedOn() != null) {
			dto.setUpdatedOn(comment.getUpdatedOn().format(DateUtil.MAIN_FORMATTER));
		}
		return dto;
	}

	private UserShortDto toUserShortDto(User user) {
		UserShortDto dto = new UserShortDto();
		dto.setId(user.getId());
		dto.setName(user.getName());
		return dto;
	}
}

