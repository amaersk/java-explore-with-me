package ru.practicum.main.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
import ru.practicum.main.model.ParticipationRequest;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.ParticipationRequestRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.ViewStatsDto;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    private final StatsClient statsClient;

    public EventService(EventRepository eventRepository, EventMapper eventMapper, UserService userService, CategoryService categoryService, ParticipationRequestRepository participationRequestRepository, StatsClient statsClient) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
        this.userService = userService;
        this.categoryService = categoryService;
        this.participationRequestRepository = participationRequestRepository;
        this.statsClient = statsClient;
    }

    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        if (size <= 0) {
            throw new BadRequestException("Size must be greater than 0");
        }
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Event> events = eventRepository.findByInitiatorId(userId, pageable);
        return events.getContent().stream().map(event -> {
            Long confirmedRequests = participationRequestRepository.countConfirmedRequestsByEventId(event.getId());
            return eventMapper.toEventShortDto(event, confirmedRequests, 0L);
        }).collect(Collectors.toList());
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

    public List<EventFullDto> getAdminEvents(List<Long> users, List<String> states, List<Long> categories, String rangeStart, String rangeEnd, Integer from, Integer size) {
        if (size <= 0) {
            throw new BadRequestException("Size must be greater than 0");
        }
        if (from < 0) {
            throw new BadRequestException("From must be greater than or equal to 0");
        }
        Pageable pageable = PageRequest.of(from / size, size);

        final List<EventState> eventStates = parseEventStates(states);

        LocalDateTime start;
        LocalDateTime end;
        try {
            start = rangeStart != null ? LocalDateTime.parse(rangeStart, FORMATTER) : null;
            end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, FORMATTER) : null;
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Incorrectly made request.");
        }

        if (start != null && end != null && start.isAfter(end)) {
            throw new BadRequestException("rangeStart must be before rangeEnd");
        }

        Specification<Event> spec = Specification.where(null);
        if (users != null && !users.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("initiator").get("id").in(users));
        }
        if (!eventStates.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("state").in(eventStates));
        }
        if (categories != null && !categories.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("category").get("id").in(categories));
        }
        if (start != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("eventDate"), start));
        }
        if (end != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("eventDate"), end));
        }

        Page<Event> events = eventRepository.findAll(spec, pageable);
        return events.getContent().stream().map(event -> {
            Long confirmedRequests = participationRequestRepository.countConfirmedRequestsByEventId(event.getId());
            return eventMapper.toEventFullDto(event, confirmedRequests, 0L);
        }).collect(Collectors.toList());
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

    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid, String rangeStart, String rangeEnd, Boolean onlyAvailable, String sort, Integer from, Integer size) {
        if (size <= 0) {
            throw new BadRequestException("Size must be greater than 0");
        }
        if (from < 0) {
            throw new BadRequestException("From must be greater than or equal to 0");
        }

        LocalDateTime parsedStart;
        LocalDateTime parsedEnd;
        try {
            parsedStart = rangeStart != null ? LocalDateTime.parse(rangeStart, FORMATTER) : null;
            parsedEnd = rangeEnd != null ? LocalDateTime.parse(rangeEnd, FORMATTER) : null;
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Incorrectly made request.");
        }

        final LocalDateTime end = parsedEnd;
        final LocalDateTime start = (parsedStart == null && end == null) ? LocalDateTime.now() : parsedStart;
        if (start != null && end != null && start.isAfter(end)) {
            throw new BadRequestException("rangeStart must be before rangeEnd");
        }

        Specification<Event> spec = Specification.where((root, query, cb) -> cb.equal(root.get("state"), EventState.PUBLISHED));
        if (text != null && !text.isBlank()) {
            String like = "%" + text.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("annotation")), like),
                    cb.like(cb.lower(root.get("description")), like)
            ));
        }
        if (categories != null && !categories.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("category").get("id").in(categories));
        }
        if (paid != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("paid"), paid));
        }
        if (start != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("eventDate"), start));
        }
        if (end != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("eventDate"), end));
        }
        if (Boolean.TRUE.equals(onlyAvailable)) {
            spec = spec.and((root, query, cb) -> {
                Subquery<Long> confirmedSubquery = query.subquery(Long.class);
                var pr = confirmedSubquery.from(ParticipationRequest.class);
                confirmedSubquery.select(cb.count(pr));
                confirmedSubquery.where(
                        cb.equal(pr.get("event").get("id"), root.get("id")),
                        cb.equal(pr.get("status"), ParticipationRequest.RequestStatus.CONFIRMED)
                );

                Expression<Long> participantLimit = root.get("participantLimit").as(Long.class);
                return cb.or(
                        cb.equal(root.get("participantLimit"), 0),
                        cb.lessThan(confirmedSubquery, participantLimit)
                );
            });
        }

        List<Event> eventList;
        if ("VIEWS".equals(sort)) {
            // We can't sort by views in DB (views are in stats service), so we sort in-memory and then apply pagination.
            eventList = eventRepository.findAll(spec);
        } else {
            Pageable pageable;
            if ("EVENT_DATE".equals(sort)) {
                pageable = PageRequest.of(from / size, size, Sort.by("eventDate"));
            } else {
                pageable = PageRequest.of(from / size, size);
            }
            eventList = eventRepository.findAll(spec, pageable).getContent();
        }

        Map<String, Long> viewsMap = getViewsMap(eventList, start, end);

        if ("VIEWS".equals(sort)) {
            eventList = eventList.stream()
                    .sorted(Comparator.<Event>comparingLong(e -> viewsMap.getOrDefault("/events/" + e.getId(), 0L)).reversed()
                            .thenComparing(Event::getId))
                    .skip(from)
                    .limit(size)
                    .collect(Collectors.toList());
        }

        return eventList.stream().map(event -> {
            Long confirmedRequests = participationRequestRepository.countConfirmedRequestsByEventId(event.getId());
            String uri = "/events/" + event.getId();
            Long views = viewsMap.getOrDefault(uri, 0L);
            return eventMapper.toEventShortDto(event, confirmedRequests, views);
        }).collect(Collectors.toList());
    }

    public EventFullDto getPublicEvent(Long id) {
        Event event = findEventById(id);
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id=" + id + " was not found");
        }
        Long confirmedRequests = participationRequestRepository.countConfirmedRequestsByEventId(id);
        Long views = getViewsForEvent(id);
        return eventMapper.toEventFullDto(event, confirmedRequests, views);
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
            if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + dto.getEventDate());
            }
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
        return eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private Map<String, Long> getViewsMap(List<Event> events, LocalDateTime start, LocalDateTime end) {
        if (events.isEmpty()) {
            return Map.of();
        }

        List<String> uris = events.stream().map(event -> "/events/" + event.getId()).collect(Collectors.toList());

        try {
            LocalDateTime statsStart = start != null ? start : LocalDateTime.now().minusYears(1);
            LocalDateTime statsEnd = end != null ? end : LocalDateTime.now().plusYears(1);

            ViewStatsDto[] stats = statsClient.getStats(statsStart, statsEnd, uris, false).getBody();
            if (stats == null) {
                return Map.of();
            }

            return Arrays.stream(stats).collect(Collectors.toMap(ViewStatsDto::getUri, ViewStatsDto::getHits, (hits1, hits2) -> hits1 + hits2));
        } catch (Exception e) {
            return Map.of();
        }
    }

    private Long getViewsForEvent(Long eventId) {
        try {
            String uri = "/events/" + eventId;
            LocalDateTime start = LocalDateTime.now().minusYears(1);
            LocalDateTime end = LocalDateTime.now().plusYears(1);
            ViewStatsDto[] stats = statsClient.getStats(start, end, List.of(uri), false).getBody();
            if (stats != null && stats.length > 0) {
                return stats[0].getHits();
            }
            return 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private List<EventState> parseEventStates(List<String> states) {
        if (states == null || states.isEmpty()) {
            return List.of();
        }
        try {
            return states.stream().map(EventState::valueOf).collect(Collectors.toList());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unknown state in states parameter");
        }
    }
}
