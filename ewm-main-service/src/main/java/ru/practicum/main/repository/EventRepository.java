package ru.practicum.main.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.practicum.main.model.Event;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
	Page<Event> findByInitiatorId(Long userId, Pageable pageable);

	List<Event> findByIdIn(List<Long> eventIds);

	boolean existsByCategoryId(Long categoryId);
}
