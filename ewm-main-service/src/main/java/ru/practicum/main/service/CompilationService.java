package ru.practicum.main.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.CompilationDto;
import ru.practicum.main.dto.EventShortDto;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.mapper.CompilationMapper;
import ru.practicum.main.mapper.EventMapper;
import ru.practicum.main.model.Compilation;
import ru.practicum.main.model.Event;
import ru.practicum.main.repository.CompilationRepository;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.ParticipationRequestRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CompilationService {
	private final CompilationRepository compilationRepository;
	private final CompilationMapper compilationMapper;
	private final EventRepository eventRepository;
	private final EventMapper eventMapper;
	private final ParticipationRequestRepository participationRequestRepository;

	public CompilationService(CompilationRepository compilationRepository, CompilationMapper compilationMapper,
	                          EventRepository eventRepository, EventMapper eventMapper,
	                          ParticipationRequestRepository participationRequestRepository) {
		this.compilationRepository = compilationRepository;
		this.compilationMapper = compilationMapper;
		this.eventRepository = eventRepository;
		this.eventMapper = eventMapper;
		this.participationRequestRepository = participationRequestRepository;
	}

	public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
		if (size <= 0) {
			throw new BadRequestException("Size must be greater than 0");
		}
		Pageable pageable = PageRequest.of(from / size, size);
		Page<Compilation> compilations;
		if (pinned != null) {
			compilations = compilationRepository.findByPinned(pinned, pageable);
		} else {
			compilations = compilationRepository.findAll(pageable);
		}

		return compilations.getContent().stream()
				.map(compilation -> {
					List<EventShortDto> events = compilation.getEvents().stream()
							.map(event -> {
								Long confirmedRequests = participationRequestRepository.countConfirmedRequestsByEventId(event.getId());
								return eventMapper.toEventShortDto(event, confirmedRequests, 0L);
							})
							.collect(Collectors.toList());
					return compilationMapper.toCompilationDto(compilation, events);
				})
				.collect(Collectors.toList());
	}

	public CompilationDto getCompilation(Long compId) {
		Compilation compilation = findCompilationById(compId);
		List<EventShortDto> events = compilation.getEvents().stream()
				.map(event -> {
					Long confirmedRequests = participationRequestRepository.countConfirmedRequestsByEventId(event.getId());
					return eventMapper.toEventShortDto(event, confirmedRequests, 0L);
				})
				.collect(Collectors.toList());
		return compilationMapper.toCompilationDto(compilation, events);
	}

	@Transactional
	public CompilationDto createCompilation(ru.practicum.main.dto.NewCompilationDto dto) {
		Compilation compilation = new Compilation();
		compilation.setTitle(dto.getTitle());
		compilation.setPinned(dto.getPinned() != null ? dto.getPinned() : false);

		if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
			List<Event> events = eventRepository.findByIdIn(dto.getEvents());
			compilation.setEvents(new java.util.HashSet<>(events));
		}

		Compilation saved = compilationRepository.save(compilation);
		List<EventShortDto> events = saved.getEvents().stream()
				.map(event -> {
					Long confirmedRequests = participationRequestRepository.countConfirmedRequestsByEventId(event.getId());
					return eventMapper.toEventShortDto(event, confirmedRequests, 0L);
				})
				.collect(Collectors.toList());
		return compilationMapper.toCompilationDto(saved, events);
	}

	@Transactional
	public CompilationDto updateCompilation(Long compId, ru.practicum.main.dto.UpdateCompilationRequest dto) {
		Compilation compilation = findCompilationById(compId);

		if (dto.getTitle() != null && !dto.getTitle().trim().isEmpty()) {
			compilation.setTitle(dto.getTitle());
		}
		if (dto.getPinned() != null) {
			compilation.setPinned(dto.getPinned());
		}
		if (dto.getEvents() != null) {
			if (dto.getEvents().isEmpty()) {
				compilation.setEvents(new java.util.HashSet<>());
			} else {
				List<Event> events = eventRepository.findByIdIn(dto.getEvents());
				compilation.setEvents(new java.util.HashSet<>(events));
			}
		}

		Compilation saved = compilationRepository.save(compilation);
		List<EventShortDto> events = saved.getEvents().stream()
				.map(event -> {
					Long confirmedRequests = participationRequestRepository.countConfirmedRequestsByEventId(event.getId());
					return eventMapper.toEventShortDto(event, confirmedRequests, 0L);
				})
				.collect(Collectors.toList());
		return compilationMapper.toCompilationDto(saved, events);
	}

	@Transactional
	public void deleteCompilation(Long compId) {
		Compilation compilation = findCompilationById(compId);
		compilationRepository.delete(compilation);
	}

	private Compilation findCompilationById(Long compId) {
		return compilationRepository.findById(compId)
				.orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));
	}
}
