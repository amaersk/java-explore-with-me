package ru.practicum.main.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.dto.EventFullDto;
import ru.practicum.main.dto.EventShortDto;
import ru.practicum.main.dto.LocationDto;
import ru.practicum.main.dto.UserShortDto;
import ru.practicum.main.model.Category;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.Location;
import ru.practicum.main.model.User;
import java.time.format.DateTimeFormatter;

@Component
public class EventMapper {
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public EventFullDto toEventFullDto(Event event, Long confirmedRequests, Long views) {
		EventFullDto dto = new EventFullDto();
		dto.setId(event.getId());
		dto.setAnnotation(event.getAnnotation());
		dto.setCategory(toCategoryDto(event.getCategory()));
		dto.setDescription(event.getDescription());
		dto.setEventDate(event.getEventDate().format(FORMATTER));
		dto.setInitiator(toUserShortDto(event.getInitiator()));
		dto.setLocation(toLocationDto(event.getLocation()));
		dto.setPaid(event.getPaid());
		dto.setParticipantLimit(event.getParticipantLimit());
		dto.setRequestModeration(event.getRequestModeration());
		dto.setState(event.getState().name());
		dto.setTitle(event.getTitle());
		dto.setCreatedOn(event.getCreatedOn().format(FORMATTER));
		if (event.getPublishedOn() != null) {
			dto.setPublishedOn(event.getPublishedOn().format(FORMATTER));
		}
		dto.setConfirmedRequests(confirmedRequests);
		dto.setViews(views);
		return dto;
	}

	public EventShortDto toEventShortDto(Event event, Long confirmedRequests, Long views) {
		EventShortDto dto = new EventShortDto();
		dto.setId(event.getId());
		dto.setAnnotation(event.getAnnotation());
		dto.setCategory(toCategoryDto(event.getCategory()));
		dto.setEventDate(event.getEventDate().format(FORMATTER));
		dto.setInitiator(toUserShortDto(event.getInitiator()));
		dto.setPaid(event.getPaid());
		dto.setTitle(event.getTitle());
		dto.setConfirmedRequests(confirmedRequests);
		dto.setViews(views);
		return dto;
	}

	public CategoryDto toCategoryDto(Category category) {
		CategoryDto dto = new CategoryDto();
		dto.setId(category.getId());
		dto.setName(category.getName());
		return dto;
	}

	public UserShortDto toUserShortDto(User user) {
		UserShortDto dto = new UserShortDto();
		dto.setId(user.getId());
		dto.setName(user.getName());
		return dto;
	}

	public LocationDto toLocationDto(Location location) {
		LocationDto dto = new LocationDto();
		dto.setLat(location.getLat());
		dto.setLon(location.getLon());
		return dto;
	}

	public Location toLocation(LocationDto dto) {
		Location location = new Location();
		location.setLat(dto.getLat());
		location.setLon(dto.getLon());
		return location;
	}
}
