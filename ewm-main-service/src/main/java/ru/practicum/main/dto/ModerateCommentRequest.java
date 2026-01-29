package ru.practicum.main.dto;

import jakarta.validation.constraints.NotBlank;

public class ModerateCommentRequest {
	@NotBlank
	private String action; // PUBLISH or REJECT

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}

