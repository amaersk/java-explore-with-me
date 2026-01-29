package ru.practicum.main.mapper;

import ru.practicum.main.dto.ParticipationRequestDto;
import ru.practicum.main.model.ParticipationRequest;
import ru.practicum.main.util.DateUtil;

public final class ParticipationRequestMapper {
	private ParticipationRequestMapper() {
	}

	public static ParticipationRequestDto toParticipationRequestDto(ParticipationRequest request) {
		ParticipationRequestDto dto = new ParticipationRequestDto();
		dto.setId(request.getId());
		dto.setEvent(request.getEvent().getId());
		dto.setRequester(request.getRequester().getId());
		dto.setStatus(request.getStatus().name());
		dto.setCreated(request.getCreated().format(DateUtil.REQUEST_CREATED_FORMATTER));
		return dto;
	}
}
