package ru.practicum.stats.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.model.EndpointHit;
import ru.practicum.stats.repository.EndpointHitRepository;

@Service
public class StatsService {
	private final EndpointHitRepository repository;

	public StatsService(EndpointHitRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public void saveHit(EndpointHitDto dto) {
		EndpointHit hit = new EndpointHit();
		hit.setApp(dto.getApp());
		hit.setUri(dto.getUri());
		hit.setIp(dto.getIp());
		hit.setTimestamp(dto.getTimestamp());
		repository.save(hit);
	}

	@Transactional(readOnly = true)
	public List<ViewStatsDto> getStats(LocalDateTime start,
	                                   LocalDateTime end,
	                                   Collection<String> uris,
	                                   boolean unique) {
		if (unique) {
			return repository.aggregateUnique(start, end, uris == null || uris.isEmpty() ? null : uris);
		}
		return repository.aggregateAll(start, end, uris == null || uris.isEmpty() ? null : uris);
	}
}


