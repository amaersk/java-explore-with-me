package ru.practicum.main.dto;

public class ParticipationRequestDto {
	private Long id;
	private Long event;
	private Long requester;
	private String status;
	private String created;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getEvent() {
		return event;
	}

	public void setEvent(Long event) {
		this.event = event;
	}

	public Long getRequester() {
		return requester;
	}

	public void setRequester(Long requester) {
		this.requester = requester;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}
}


