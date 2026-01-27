package ru.practicum.stats.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.model.EndpointHit;

public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {
	@Query(
			"SELECT new ru.practicum.stats.dto.ViewStatsDto(e.app, e.uri, COUNT(e.id)) " +
			"FROM EndpointHit e " +
			"WHERE e.timestamp BETWEEN :start AND :end " +
			"AND (:uris IS NULL OR e.uri IN :uris) " +
			"GROUP BY e.app, e.uri " +
			"ORDER BY COUNT(e.id) DESC"
	)
	List<ViewStatsDto> aggregateAll(@Param("start") LocalDateTime start,
	                                @Param("end") LocalDateTime end,
	                                @Param("uris") Collection<String> uris);

	@Query(
			"SELECT new ru.practicum.stats.dto.ViewStatsDto(e.app, e.uri, COUNT(DISTINCT e.ip)) " +
			"FROM EndpointHit e " +
			"WHERE e.timestamp BETWEEN :start AND :end " +
			"AND (:uris IS NULL OR e.uri IN :uris) " +
			"GROUP BY e.app, e.uri " +
			"ORDER BY COUNT(DISTINCT e.ip) DESC"
	)
	List<ViewStatsDto> aggregateUnique(@Param("start") LocalDateTime start,
	                                   @Param("end") LocalDateTime end,
	                                   @Param("uris") Collection<String> uris);
}


