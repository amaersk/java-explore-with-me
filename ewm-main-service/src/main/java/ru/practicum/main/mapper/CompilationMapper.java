package ru.practicum.main.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.main.dto.CompilationDto;
import ru.practicum.main.dto.EventShortDto;
import ru.practicum.main.model.Compilation;
import ru.practicum.main.model.Event;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CompilationMapper {
	private final EventMapper eventMapper;

	public CompilationMapper(EventMapper eventMapper) {
		this.eventMapper = eventMapper;
	}

	public CompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> events) {
		CompilationDto dto = new CompilationDto();
		dto.setId(compilation.getId());
		dto.setTitle(compilation.getTitle());
		dto.setPinned(compilation.getPinned());
		dto.setEvents(events);
		return dto;
	}
}
