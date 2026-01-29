package ru.practicum.main.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.UserDto;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.mapper.UserMapper;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserService {
	private final UserRepository userRepository;
	private final UserMapper userMapper;

	public UserService(UserRepository userRepository, UserMapper userMapper) {
		this.userRepository = userRepository;
		this.userMapper = userMapper;
	}

	public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
		if (size <= 0) {
			throw new BadRequestException("Size must be greater than 0");
		}
		Pageable pageable = PageRequest.of(from / size, size);
		Page<User> users;
		if (ids != null && !ids.isEmpty()) {
			users = userRepository.findByIdIn(ids, pageable);
		} else {
			users = userRepository.findAll(pageable);
		}
		return users.getContent().stream()
				.map(userMapper::toUserDto)
				.collect(Collectors.toList());
	}

	@Transactional
	public UserDto createUser(ru.practicum.main.dto.NewUserRequest dto) {
		if (userRepository.existsByEmail(dto.getEmail())) {
			throw new ConflictException("Email already exists");
		}
		User user = userMapper.toUser(dto);
		User saved = userRepository.save(user);
		return userMapper.toUserDto(saved);
	}

	@Transactional
	public void deleteUser(Long userId) {
		if (!userRepository.existsById(userId)) {
			throw new NotFoundException("User with id=" + userId + " was not found");
		}
		userRepository.deleteById(userId);
	}

	public User findUserById(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
	}
}
