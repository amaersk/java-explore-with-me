package ru.practicum.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.main.model.ParticipationRequest;
import ru.practicum.main.model.ParticipationRequest.RequestStatus;
import java.util.List;

@Repository
public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
	List<ParticipationRequest> findByRequesterId(Long userId);

	List<ParticipationRequest> findByEventId(Long eventId);

	List<ParticipationRequest> findByEventInitiatorIdAndEventId(Long userId, Long eventId);

	@Query("SELECT COUNT(pr) FROM ParticipationRequest pr WHERE pr.event.id = :eventId AND pr.status = 'CONFIRMED'")
	Long countConfirmedRequestsByEventId(@Param("eventId") Long eventId);

	boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

	List<ParticipationRequest> findByIdIn(List<Long> requestIds);
}
