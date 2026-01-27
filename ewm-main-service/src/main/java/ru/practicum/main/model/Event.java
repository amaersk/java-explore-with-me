package ru.practicum.main.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 2000)
	private String annotation;

	@ManyToOne
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	@Column(nullable = false, length = 7000)
	private String description;

	@Column(name = "event_date", nullable = false)
	private LocalDateTime eventDate;

	@ManyToOne
	@JoinColumn(name = "initiator_id", nullable = false)
	private User initiator;

	@Embedded
	private Location location;

	@Column(nullable = false)
	private Boolean paid = false;

	@Column(name = "participant_limit", nullable = false)
	private Integer participantLimit = 0;

	@Column(name = "request_moderation", nullable = false)
	private Boolean requestModeration = true;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EventState state = EventState.PENDING;

	@Column(nullable = false, length = 120)
	private String title;

	@Column(name = "created_on", nullable = false)
	private LocalDateTime createdOn;

	@Column(name = "published_on")
	private LocalDateTime publishedOn;

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

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDateTime getEventDate() {
		return eventDate;
	}

	public void setEventDate(LocalDateTime eventDate) {
		this.eventDate = eventDate;
	}

	public User getInitiator() {
		return initiator;
	}

	public void setInitiator(User initiator) {
		this.initiator = initiator;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Boolean getPaid() {
		return paid;
	}

	public void setPaid(Boolean paid) {
		this.paid = paid;
	}

	public Integer getParticipantLimit() {
		return participantLimit;
	}

	public void setParticipantLimit(Integer participantLimit) {
		this.participantLimit = participantLimit;
	}

	public Boolean getRequestModeration() {
		return requestModeration;
	}

	public void setRequestModeration(Boolean requestModeration) {
		this.requestModeration = requestModeration;
	}

	public EventState getState() {
		return state;
	}

	public void setState(EventState state) {
		this.state = state;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public LocalDateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(LocalDateTime createdOn) {
		this.createdOn = createdOn;
	}

	public LocalDateTime getPublishedOn() {
		return publishedOn;
	}

	public void setPublishedOn(LocalDateTime publishedOn) {
		this.publishedOn = publishedOn;
	}

	public enum EventState {
		PENDING, PUBLISHED, CANCELED
	}
}
