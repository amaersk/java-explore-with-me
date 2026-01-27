package ru.practicum.main.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class NewCompilationDto {
	private List<Long> events;

	private Boolean pinned;

	@NotBlank
	@Size(min = 1, max = 50)
	private String title;

	public List<Long> getEvents() {
		return events;
	}

	public void setEvents(List<Long> events) {
		this.events = events;
	}

	public Boolean getPinned() {
		return pinned;
	}

	public void setPinned(Boolean pinned) {
		this.pinned = pinned;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}


