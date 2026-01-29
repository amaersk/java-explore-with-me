package ru.practicum.main.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.dto.ParticipationRequestDto;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.mapper.ParticipationRequestMapper;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.Event.EventState;
import ru.practicum.main.model.ParticipationRequest;
import ru.practicum.main.model.ParticipationRequest.RequestStatus;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.ParticipationRequestRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ParticipationRequestService {
	private final ParticipationRequestRepository participationRequestRepository;
	private final EventService eventService;
	private final UserService userService;

	public ParticipationRequestService(ParticipationRequestRepository participationRequestRepository,
	                                   EventService eventService, UserService userService) {
		this.participationRequestRepository = participationRequestRepository;
		this.eventService = eventService;
		this.userService = userService;
	}

	public List<ParticipationRequestDto> getUserRequests(Long userId) {
		User user = userService.findUserById(userId);
		List<ParticipationRequest> requests = participationRequestRepository.findByRequesterId(userId);
		return requests.stream()
				.map(ParticipationRequestMapper::toParticipationRequestDto)
				.collect(Collectors.toList());
	}

	@Transactional
	public ParticipationRequestDto createRequest(Long userId, Long eventId) {
		User user = userService.findUserById(userId);
		Event event = eventService.findEventById(eventId);

		if (event.getInitiator().getId().equals(userId)) {
			throw new ConflictException("Initiator cannot request participation in own event");
		}

		if (event.getState() != EventState.PUBLISHED) {
			throw new ConflictException("Event must be published");
		}

		if (participationRequestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
			throw new ConflictException("Request already exists");
		}

		Long confirmedRequests = participationRequestRepository.countConfirmedRequestsByEventId(eventId);
		if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {
			throw new ConflictException("Participant limit reached");
		}

		ParticipationRequest request = new ParticipationRequest();
		request.setEvent(event);
		request.setRequester(user);
		request.setCreated(LocalDateTime.now());

		if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
			request.setStatus(RequestStatus.CONFIRMED);
		} else {
			request.setStatus(RequestStatus.PENDING);
		}

		ParticipationRequest saved = participationRequestRepository.save(request);
		return ParticipationRequestMapper.toParticipationRequestDto(saved);
	}

	@Transactional
	public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
		ParticipationRequest request = participationRequestRepository.findById(requestId)
				.orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

		if (!request.getRequester().getId().equals(userId)) {
			throw new NotFoundException("Request with id=" + requestId + " was not found");
		}

		request.setStatus(RequestStatus.CANCELED);
		ParticipationRequest saved = participationRequestRepository.save(request);
		return ParticipationRequestMapper.toParticipationRequestDto(saved);
	}

	public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
		Event event = eventService.findEventById(eventId);
		if (!event.getInitiator().getId().equals(userId)) {
			throw new NotFoundException("Event with id=" + eventId + " was not found");
		}

		List<ParticipationRequest> requests = participationRequestRepository.findByEventInitiatorIdAndEventId(userId, eventId);
		return requests.stream()
				.map(ParticipationRequestMapper::toParticipationRequestDto)
				.collect(Collectors.toList());
	}

	@Transactional
	public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest dto) {
		Event event = eventService.findEventById(eventId);
		if (!event.getInitiator().getId().equals(userId)) {
			throw new NotFoundException("Event with id=" + eventId + " was not found");
		}

		if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
			throw new BadRequestException("Event does not require moderation");
		}

		List<ParticipationRequest> requests = participationRequestRepository.findByIdIn(dto.getRequestIds());
		for (ParticipationRequest request : requests) {
			if (!request.getEvent().getId().equals(eventId)) {
				throw new BadRequestException("Request does not belong to this event");
			}
			if (request.getStatus() != RequestStatus.PENDING) {
				throw new ConflictException("Request must have status PENDING");
			}
		}

		Long confirmedCount = participationRequestRepository.countConfirmedRequestsByEventId(eventId);
		EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();

		if ("CONFIRMED".equals(dto.getStatus())) {
			for (ParticipationRequest request : requests) {
				if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
					throw new ConflictException("The participant limit has been reached");
				}
				request.setStatus(RequestStatus.CONFIRMED);
				confirmedCount++;
			}
			participationRequestRepository.saveAll(requests);

			if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
				List<ParticipationRequest> pendingRequests = participationRequestRepository.findByEventId(eventId)
						.stream()
						.filter(r -> r.getStatus() == RequestStatus.PENDING)
						.collect(Collectors.toList());
				for (ParticipationRequest pending : pendingRequests) {
					pending.setStatus(RequestStatus.REJECTED);
				}
				participationRequestRepository.saveAll(pendingRequests);
			}

			result.setConfirmedRequests(requests.stream()
					.map(ParticipationRequestMapper::toParticipationRequestDto)
					.collect(Collectors.toList()));
		} else if ("REJECTED".equals(dto.getStatus())) {
			for (ParticipationRequest request : requests) {
				request.setStatus(RequestStatus.REJECTED);
			}
			participationRequestRepository.saveAll(requests);
			result.setRejectedRequests(requests.stream()
					.map(ParticipationRequestMapper::toParticipationRequestDto)
					.collect(Collectors.toList()));
		}

		return result;
	}
}
