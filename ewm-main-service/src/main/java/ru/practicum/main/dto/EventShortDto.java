package ru.practicum.main.dto;

public class EventShortDto {
	private Long id;
	private String annotation;
	private CategoryDto category;
	private String eventDate;
	private UserShortDto initiator;
	private Boolean paid;
	private String title;
	private Long confirmedRequests;
	private Long views;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public CategoryDto getCategory() {
		return category;
	}

	public void setCategory(CategoryDto category) {
		this.category = category;
	}

	public String getEventDate() {
		return eventDate;
	}

	public void setEventDate(String eventDate) {
		this.eventDate = eventDate;
	}

	public UserShortDto getInitiator() {
		return initiator;
	}

	public void setInitiator(UserShortDto initiator) {
		this.initiator = initiator;
	}

	public Boolean getPaid() {
		return paid;
	}

	public void setPaid(Boolean paid) {
		this.paid = paid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getConfirmedRequests() {
		return confirmedRequests;
	}

	public void setConfirmedRequests(Long confirmedRequests) {
		this.confirmedRequests = confirmedRequests;
	}

	public Long getViews() {
		return views;
	}

	public void setViews(Long views) {
		this.views = views;
	}
}


