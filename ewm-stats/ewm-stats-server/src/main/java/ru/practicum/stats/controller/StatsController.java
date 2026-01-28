package ru.practicum.stats.controller;

import jakarta.validation.Valid;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.service.StatsService;

@RestController
public class StatsController {
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private final StatsService statsService;

	public StatsController(StatsService statsService) {
		this.statsService = statsService;
	}

	@PostMapping("/hit")
	public ResponseEntity<Void> hit(@Valid @RequestBody EndpointHitDto dto) {
		statsService.saveHit(dto);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@GetMapping("/stats")
	public ResponseEntity<List<ViewStatsDto>> getStats(@RequestParam("start") String start,
	                                                  @RequestParam("end") String end,
	                                                  @RequestParam(value = "uris", required = false) List<String> uris,
	                                                  @RequestParam(value = "unique", defaultValue = "false") boolean unique) {
		LocalDateTime startDt;
		LocalDateTime endDt;
		try {
			startDt = LocalDateTime.parse(URLDecoder.decode(start, StandardCharsets.UTF_8), FORMATTER);
			endDt = LocalDateTime.parse(URLDecoder.decode(end, StandardCharsets.UTF_8), FORMATTER);
		} catch (DateTimeParseException ex) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

		if (startDt.isAfter(endDt)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

		return ResponseEntity.ok(statsService.getStats(startDt, endDt, uris, unique));
	}
}


