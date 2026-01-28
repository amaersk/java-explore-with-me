package ru.practicum.main.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.main.model.Event;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
	Page<Event> findByInitiatorId(Long userId, Pageable pageable);

	@Query(value = "SELECT e.* FROM events e " +
			"WHERE (COALESCE(array_length(:users::bigint[], 1), 0) = 0 OR e.initiator_id = ANY(:users::bigint[])) " +
			"AND (COALESCE(array_length(:states::text[], 1), 0) = 0 OR e.state = ANY(:states::text[])) " +
			"AND (COALESCE(array_length(:categories::bigint[], 1), 0) = 0 OR e.category_id = ANY(:categories::bigint[])) " +
			"AND (:rangeStart IS NULL OR e.event_date >= :rangeStart) " +
			"AND (:rangeEnd IS NULL OR e.event_date <= :rangeEnd)",
			nativeQuery = true)
	Page<Event> findEventsByAdminFilters(
			@Param("users") List<Long> users,
			@Param("states") List<String> states,
			@Param("categories") List<Long> categories,
			@Param("rangeStart") LocalDateTime rangeStart,
			@Param("rangeEnd") LocalDateTime rangeEnd,
			Pageable pageable
	);

	@Query(value = "SELECT e.* FROM events e " +
			"WHERE e.state = 'PUBLISHED' " +
			"AND (:text IS NULL OR :text = '' OR LOWER(e.annotation) LIKE LOWER('%' || :text || '%') OR LOWER(e.description) LIKE LOWER('%' || :text || '%')) " +
			"AND (COALESCE(array_length(:categories::bigint[], 1), 0) = 0 OR e.category_id = ANY(:categories::bigint[])) " +
			"AND (:paid IS NULL OR e.paid = :paid) " +
			"AND (:rangeStart IS NULL OR e.event_date >= :rangeStart) " +
			"AND (:rangeEnd IS NULL OR e.event_date <= :rangeEnd) " +
			"AND (:onlyAvailable IS NULL OR :onlyAvailable = false OR " +
			"(e.participant_limit = 0 OR (SELECT COUNT(*) FROM participation_requests pr WHERE pr.event_id = e.id AND pr.status = 'CONFIRMED') < e.participant_limit))",
			nativeQuery = true)
	Page<Event> findPublicEvents(
			@Param("text") String text,
			@Param("categories") List<Long> categories,
			@Param("paid") Boolean paid,
			@Param("rangeStart") LocalDateTime rangeStart,
			@Param("rangeEnd") LocalDateTime rangeEnd,
			@Param("onlyAvailable") Boolean onlyAvailable,
			Pageable pageable
	);

	List<Event> findByIdIn(List<Long> eventIds);

	boolean existsByCategoryId(Long categoryId);
}
