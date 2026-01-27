package ru.practicum.main.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.main.dto.UserDto;
import ru.practicum.main.model.User;

@Component
public class UserMapper {
	public UserDto toUserDto(User user) {
		UserDto dto = new UserDto();
		dto.setId(user.getId());
		dto.setEmail(user.getEmail());
		dto.setName(user.getName());
		return dto;
	}

	public User toUser(ru.practicum.main.dto.NewUserRequest dto) {
		User user = new User();
		user.setEmail(dto.getEmail());
		user.setName(dto.getName());
		return user;
	}
}
