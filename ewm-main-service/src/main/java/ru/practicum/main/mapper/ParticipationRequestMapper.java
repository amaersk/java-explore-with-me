package ru.practicum.main.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.main.dto.ParticipationRequestDto;
import ru.practicum.main.model.ParticipationRequest;
import java.time.format.DateTimeFormatter;

@Component
public class ParticipationRequestMapper {
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

	public ParticipationRequestDto toParticipationRequestDto(ParticipationRequest request) {
		ParticipationRequestDto dto = new ParticipationRequestDto();
		dto.setId(request.getId());
		dto.setEvent(request.getEvent().getId());
		dto.setRequester(request.getRequester().getId());
		dto.setStatus(request.getStatus().name());
		dto.setCreated(request.getCreated().format(FORMATTER));
		return dto;
	}
}
