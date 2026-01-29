package ru.practicum.main.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.dto.EventFullDto;
import ru.practicum.main.dto.EventShortDto;
import ru.practicum.main.dto.LocationDto;
import ru.practicum.main.dto.NewEventDto;
import ru.practicum.main.dto.UserShortDto;
import ru.practicum.main.model.Category;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.Location;
import ru.practicum.main.model.User;
import ru.practicum.main.util.DateUtil;
import java.time.LocalDateTime;

@Component
public class EventMapper {
	public EventFullDto toEventFullDto(Event event, Long confirmedRequests, Long views) {
		EventFullDto dto = new EventFullDto();
		dto.setId(event.getId());
		dto.setAnnotation(event.getAnnotation());
		dto.setCategory(toCategoryDto(event.getCategory()));
		dto.setDescription(event.getDescription());
		dto.setEventDate(event.getEventDate().format(DateUtil.MAIN_FORMATTER));
		dto.setInitiator(toUserShortDto(event.getInitiator()));
		dto.setLocation(toLocationDto(event.getLocation()));
		dto.setPaid(event.getPaid());
		dto.setParticipantLimit(event.getParticipantLimit());
		dto.setRequestModeration(event.getRequestModeration());
		dto.setState(event.getState().name());
		dto.setTitle(event.getTitle());
		dto.setCreatedOn(event.getCreatedOn().format(DateUtil.MAIN_FORMATTER));
		if (event.getPublishedOn() != null) {
			dto.setPublishedOn(event.getPublishedOn().format(DateUtil.MAIN_FORMATTER));
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
		dto.setEventDate(event.getEventDate().format(DateUtil.MAIN_FORMATTER));
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

	public Event toEvent(NewEventDto dto, User initiator, Category category, LocalDateTime eventDate, LocalDateTime createdOn) {
		Event event = new Event();
		event.setAnnotation(dto.getAnnotation());
		event.setCategory(category);
		event.setDescription(dto.getDescription());
		event.setEventDate(eventDate);
		event.setInitiator(initiator);
		event.setLocation(toLocation(dto.getLocation()));
		event.setPaid(dto.getPaid() != null ? dto.getPaid() : false);
		event.setParticipantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0);
		event.setRequestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true);
		event.setState(Event.EventState.PENDING);
		event.setTitle(dto.getTitle());
		event.setCreatedOn(createdOn);
		return event;
	}
}
