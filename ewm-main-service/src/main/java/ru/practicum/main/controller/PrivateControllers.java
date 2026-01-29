package ru.practicum.main.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import ru.practicum.main.dto.EventFullDto;
import ru.practicum.main.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.dto.EventShortDto;
import ru.practicum.main.dto.NewEventDto;
import ru.practicum.main.dto.ParticipationRequestDto;
import ru.practicum.main.dto.UpdateEventUserRequest;
import ru.practicum.main.service.EventService;
import ru.practicum.main.service.ParticipationRequestService;

@RestController
@RequestMapping("/users/{userId}")
public class PrivateControllers {
	private final EventService eventService;
	private final ParticipationRequestService participationRequestService;

	public PrivateControllers(EventService eventService, ParticipationRequestService participationRequestService) {
		this.eventService = eventService;
		this.participationRequestService = participationRequestService;
	}

	@GetMapping("/events")
	public List<EventShortDto> getEvents(@PathVariable("userId") Long userId,
	                                     @RequestParam(value = "from", defaultValue = "0") Integer from,
	                                     @RequestParam(value = "size", defaultValue = "10") Integer size) {
		return eventService.getUserEvents(userId, from, size);
	}

	@PostMapping("/events")
	public ResponseEntity<EventFullDto> addEvent(@PathVariable("userId") Long userId,
	                                             @Valid @RequestBody NewEventDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(userId, dto));
	}

	@GetMapping("/events/{eventId}")
	public ResponseEntity<EventFullDto> getEvent(@PathVariable("userId") Long userId,
	                                             @PathVariable("eventId") Long eventId) {
		return ResponseEntity.ok(eventService.getUserEvent(userId, eventId));
	}

	@PatchMapping("/events/{eventId}")
	public ResponseEntity<EventFullDto> updateEvent(@PathVariable("userId") Long userId,
	                                                @PathVariable("eventId") Long eventId,
	                                                @Valid @RequestBody UpdateEventUserRequest dto) {
		return ResponseEntity.ok(eventService.updateEventByUser(userId, eventId, dto));
	}

	@GetMapping("/events/{eventId}/requests")
	public List<ParticipationRequestDto> getEventParticipants(@PathVariable("userId") Long userId,
	                                                          @PathVariable("eventId") Long eventId) {
		return participationRequestService.getEventRequests(userId, eventId);
	}

	@PatchMapping("/events/{eventId}/requests")
	public ResponseEntity<EventRequestStatusUpdateResult> changeRequestStatus(@PathVariable("userId") Long userId,
	                                                                          @PathVariable("eventId") Long eventId,
	                                                                          @RequestBody EventRequestStatusUpdateRequest dto) {
		return ResponseEntity.ok(participationRequestService.updateRequestStatus(userId, eventId, dto));
	}

	@GetMapping("/requests")
	public List<ParticipationRequestDto> getUserRequests(@PathVariable("userId") Long userId) {
		return participationRequestService.getUserRequests(userId);
	}

	@PostMapping("/requests")
	public ResponseEntity<ParticipationRequestDto> addParticipationRequest(@PathVariable("userId") Long userId,
	                                                                       @RequestParam("eventId") Long eventId) {
		return ResponseEntity.status(HttpStatus.CREATED).body(participationRequestService.createRequest(userId, eventId));
	}

	@PatchMapping("/requests/{requestId}/cancel")
	public ResponseEntity<ParticipationRequestDto> cancelRequest(@PathVariable("userId") Long userId,
	                                                             @PathVariable("requestId") Long requestId) {
		return ResponseEntity.ok(participationRequestService.cancelRequest(userId, requestId));
	}
}


