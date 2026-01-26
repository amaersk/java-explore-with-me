package ru.practicum.main.dto;

import java.util.List;

public class EventRequestStatusUpdateResult {
	private List<ParticipationRequestDto> confirmedRequests;
	private List<ParticipationRequestDto> rejectedRequests;

	public List<ParticipationRequestDto> getConfirmedRequests() {
		return confirmedRequests;
	}

	public void setConfirmedRequests(List<ParticipationRequestDto> confirmedRequests) {
		this.confirmedRequests = confirmedRequests;
	}

	public List<ParticipationRequestDto> getRejectedRequests() {
		return rejectedRequests;
	}

	public void setRejectedRequests(List<ParticipationRequestDto> rejectedRequests) {
		this.rejectedRequests = rejectedRequests;
	}
}


