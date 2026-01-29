package ru.practicum.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.main.model.ParticipationRequest;
import java.util.List;

@Repository
public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
	interface ConfirmedCount {
		Long getEventId();

		Long getCnt();
	}

	List<ParticipationRequest> findByRequesterId(Long userId);

	List<ParticipationRequest> findByEventId(Long eventId);

	List<ParticipationRequest> findByEventInitiatorIdAndEventId(Long userId, Long eventId);

	@Query("SELECT COUNT(pr) FROM ParticipationRequest pr WHERE pr.event.id = :eventId AND pr.status = 'CONFIRMED'")
	Long countConfirmedRequestsByEventId(@Param("eventId") Long eventId);

	@Query("SELECT pr.event.id as eventId, COUNT(pr) as cnt " +
			"FROM ParticipationRequest pr " +
			"WHERE pr.status = 'CONFIRMED' AND pr.event.id IN :eventIds " +
			"GROUP BY pr.event.id")
	List<ConfirmedCount> countConfirmedRequestsByEventIds(@Param("eventIds") List<Long> eventIds);

	boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

	List<ParticipationRequest> findByIdIn(List<Long> requestIds);
}
