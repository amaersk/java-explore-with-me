package ru.practicum.main.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.EventFullDto;
import ru.practicum.main.dto.EventShortDto;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.mapper.EventMapper;
import ru.practicum.main.model.Category;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.Event.EventState;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.ParticipationRequestRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class EventService {
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private final EventRepository eventRepository;
	private final EventMapper eventMapper;
	private final UserService userService;
	private final CategoryService categoryService;
	private final ParticipationRequestRepository participationRequestRepository;

	public EventService(EventRepository eventRepository, EventMapper eventMapper,
	                    UserService userService, CategoryService categoryService,
	                    ParticipationRequestRepository participationRequestRepository) {
		this.eventRepository = eventRepository;
		this.eventMapper = eventMapper;
		this.userService = userService;
		this.categoryService = categoryService;
		this.participationRequestRepository = participationRequestRepository;
	}

	public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
		if (size <= 0) {
			throw new BadRequestException("Size must be greater than 0");
		}
		Pageable pageable = PageRequest.of(from / size, size);
		Page<Event> events = eventRepository.findByInitiatorId(userId, pageable);
		return events.getContent().stream()
				.map(event -> {
					Long confirmedRequests = participationRequestRepository.countConfirmedRequestsByEventId(event.getId());
					return eventMapper.toEventShortDto(event, confirmedRequests, 0L);
				})
				.collect(Collectors.toList());
	}

	public EventFullDto getUserEvent(Long userId, Long eventId) {
		Event event = findEventById(eventId);
		if (!event.getInitiator().getId().equals(userId)) {
			throw new NotFoundException("Event with id=" + eventId + " was not found");
		}
		Long confirmedRequests = participationRequestRepository.countConfirmedRequestsByEventId(eventId);
		return eventMapper.toEventFullDto(event, confirmedRequests, 0L);
	}

	@Transactional
	public EventFullDto createEvent(Long userId, ru.practicum.main.dto.NewEventDto dto) {
		User user = userService.findUserById(userId);
		Category category = categoryService.findCategoryById(dto.getCategory());
		LocalDateTime eventDate = LocalDateTime.parse(dto.getEventDate(), FORMATTER);

		if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
			throw new BadRequestException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + dto.getEventDate());
		}

		Event event = new Event();
		event.setAnnotation(dto.getAnnotation());
		event.setCategory(category);
		event.setDescription(dto.getDescription());
		event.setEventDate(eventDate);
		event.setInitiator(user);
		event.setLocation(eventMapper.toLocation(dto.getLocation()));
		event.setPaid(dto.getPaid() != null ? dto.getPaid() : false);
		event.setParticipantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0);
		event.setRequestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true);
		event.setState(EventState.PENDING);
		event.setTitle(dto.getTitle());
		event.setCreatedOn(LocalDateTime.now());

		Event saved = eventRepository.save(event);
		return eventMapper.toEventFullDto(saved, 0L, 0L);
	}

	@Transactional
	public EventFullDto updateEventByUser(Long userId, Long eventId, ru.practicum.main.dto.UpdateEventUserRequest dto) {
		Event event = findEventById(eventId);
		if (!event.getInitiator().getId().equals(userId)) {
			throw new NotFoundException("Event with id=" + eventId + " was not found");
		}

		if (event.getState() == EventState.PUBLISHED) {
			throw new ConflictException("Only pending or canceled events can be changed");
		}

		updateEventFields(event, dto);

		if (dto.getStateAction() != null) {
			if ("SEND_TO_REVIEW".equals(dto.getStateAction())) {
				event.setState(EventState.PENDING);
			} else if ("CANCEL_REVIEW".equals(dto.getStateAction())) {
				event.setState(EventState.CANCELED);
			}
		}

		Event saved = eventRepository.save(event);
		Long confirmedRequests = participationRequestRepository.countConfirmedRequestsByEventId(eventId);
		return eventMapper.toEventFullDto(saved, confirmedRequests, 0L);
	}

	public List<EventFullDto> getAdminEvents(List<Long> users, List<String> states, List<Long> categories,
	                                         String rangeStart, String rangeEnd, Integer from, Integer size) {
		if (size <= 0) {
			throw new BadRequestException("Size must be greater than 0");
		}
		Pageable pageable = PageRequest.of(from / size, size);
		List<EventState> eventStates = (states != null && !states.isEmpty()) ? states.stream()
				.map(EventState::valueOf)
				.collect(Collectors.toList()) : null;
		LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, FORMATTER) : null;
		LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, FORMATTER) : null;

		List<Long> usersList = (users != null && !users.isEmpty()) ? users : null;
		List<Long> categoriesList = (categories != null && !categories.isEmpty()) ? categories : null;

		Page<Event> events = eventRepository.findEventsByAdminFilters(usersList, eventStates, categoriesList, start, end, pageable);
		return events.getContent().stream()
				.map(event -> {
					Long confirmedRequests = participationRequestRepository.countConfirmedRequestsByEventId(event.getId());
					return eventMapper.toEventFullDto(event, confirmedRequests, 0L);
				})
				.collect(Collectors.toList());
	}

	@Transactional
	public EventFullDto updateEventByAdmin(Long eventId, ru.practicum.main.dto.UpdateEventAdminRequest dto) {
		Event event = findEventById(eventId);

		if (dto.getStateAction() != null) {
			if ("PUBLISH_EVENT".equals(dto.getStateAction())) {
				if (event.getState() != EventState.PENDING) {
					throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.getState());
				}
				event.setState(EventState.PUBLISHED);
				event.setPublishedOn(LocalDateTime.now());
			} else if ("REJECT_EVENT".equals(dto.getStateAction())) {
				if (event.getState() == EventState.PUBLISHED) {
					throw new ConflictException("Cannot reject the event because it's already published");
				}
				event.setState(EventState.CANCELED);
			}
		}

		updateEventFields(event, dto);

		Event saved = eventRepository.save(event);
		Long confirmedRequests = participationRequestRepository.countConfirmedRequestsByEventId(eventId);
		return eventMapper.toEventFullDto(saved, confirmedRequests, 0L);
	}

	public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
	                                           String rangeStart, String rangeEnd, Boolean onlyAvailable,
	                                           String sort, Integer from, Integer size) {
		if (size <= 0) {
			throw new BadRequestException("Size must be greater than 0");
		}
		Pageable pageable;
		if ("EVENT_DATE".equals(sort)) {
			pageable = PageRequest.of(from / size, size, Sort.by("eventDate"));
		} else if ("VIEWS".equals(sort)) {
			pageable = PageRequest.of(from / size, size, Sort.by("eventDate").descending());
		} else {
			pageable = PageRequest.of(from / size, size);
		}

		LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, FORMATTER) : null;
		LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, FORMATTER) : null;
		if (start == null && end == null) {
			start = LocalDateTime.now();
		}

		List<Long> categoriesList = (categories != null && !categories.isEmpty()) ? categories : null;

		Page<Event> events = eventRepository.findPublicEvents(text, categoriesList, paid, start, end, onlyAvailable, pageable);
		return events.getContent().stream()
				.map(event -> {
					Long confirmedRequests = participationRequestRepository.countConfirmedRequestsByEventId(event.getId());
					return eventMapper.toEventShortDto(event, confirmedRequests, 0L);
				})
				.collect(Collectors.toList());
	}

	public EventFullDto getPublicEvent(Long id) {
		Event event = findEventById(id);
		if (event.getState() != EventState.PUBLISHED) {
			throw new NotFoundException("Event with id=" + id + " was not found");
		}
		Long confirmedRequests = participationRequestRepository.countConfirmedRequestsByEventId(id);
		return eventMapper.toEventFullDto(event, confirmedRequests, 0L);
	}

	private void updateEventFields(Event event, ru.practicum.main.dto.UpdateEventUserRequest dto) {
		if (dto.getAnnotation() != null && !dto.getAnnotation().trim().isEmpty()) {
			event.setAnnotation(dto.getAnnotation());
		}
		if (dto.getCategory() != null) {
			Category category = categoryService.findCategoryById(dto.getCategory());
			event.setCategory(category);
		}
		if (dto.getDescription() != null && !dto.getDescription().trim().isEmpty()) {
			event.setDescription(dto.getDescription());
		}
		if (dto.getEventDate() != null && !dto.getEventDate().trim().isEmpty()) {
			LocalDateTime eventDate = LocalDateTime.parse(dto.getEventDate(), FORMATTER);
			if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
				throw new BadRequestException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + dto.getEventDate());
			}
			event.setEventDate(eventDate);
		}
		if (dto.getLocation() != null) {
			event.setLocation(eventMapper.toLocation(dto.getLocation()));
		}
		if (dto.getPaid() != null) {
			event.setPaid(dto.getPaid());
		}
		if (dto.getParticipantLimit() != null) {
			event.setParticipantLimit(dto.getParticipantLimit());
		}
		if (dto.getRequestModeration() != null) {
			event.setRequestModeration(dto.getRequestModeration());
		}
		if (dto.getTitle() != null && !dto.getTitle().trim().isEmpty()) {
			event.setTitle(dto.getTitle());
		}
	}

	private void updateEventFields(Event event, ru.practicum.main.dto.UpdateEventAdminRequest dto) {
		if (dto.getAnnotation() != null && !dto.getAnnotation().trim().isEmpty()) {
			event.setAnnotation(dto.getAnnotation());
		}
		if (dto.getCategory() != null) {
			Category category = categoryService.findCategoryById(dto.getCategory());
			event.setCategory(category);
		}
		if (dto.getDescription() != null && !dto.getDescription().trim().isEmpty()) {
			event.setDescription(dto.getDescription());
		}
		if (dto.getEventDate() != null && !dto.getEventDate().trim().isEmpty()) {
			LocalDateTime eventDate = LocalDateTime.parse(dto.getEventDate(), FORMATTER);
			if (event.getPublishedOn() != null && eventDate.isBefore(event.getPublishedOn().plusHours(1))) {
				throw new ConflictException("Cannot change event date because it's less than 1 hour before publication");
			}
			event.setEventDate(eventDate);
		}
		if (dto.getLocation() != null) {
			event.setLocation(eventMapper.toLocation(dto.getLocation()));
		}
		if (dto.getPaid() != null) {
			event.setPaid(dto.getPaid());
		}
		if (dto.getParticipantLimit() != null) {
			event.setParticipantLimit(dto.getParticipantLimit());
		}
		if (dto.getRequestModeration() != null) {
			event.setRequestModeration(dto.getRequestModeration());
		}
		if (dto.getTitle() != null && !dto.getTitle().trim().isEmpty()) {
			event.setTitle(dto.getTitle());
		}
	}

	public Event findEventById(Long eventId) {
		return eventRepository.findById(eventId)
				.orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
	}
}
