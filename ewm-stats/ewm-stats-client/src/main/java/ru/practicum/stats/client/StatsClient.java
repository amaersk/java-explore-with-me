package ru.practicum.stats.client;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

@Component
public class StatsClient {
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private final RestTemplate restTemplate = new RestTemplate();
	private final String baseUrl;

	public StatsClient(@Value("${stats.server-url:http://localhost:9090}") String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void sendHit(String app, String uri, String ip, LocalDateTime timestamp) {
		EndpointHitDto dto = new EndpointHitDto();
		dto.setApp(app);
		dto.setUri(uri);
		dto.setIp(ip);
		dto.setTimestamp(timestamp);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		restTemplate.exchange(baseUrl + "/hit", HttpMethod.POST, new HttpEntity<>(dto, headers), Void.class);
	}

	public ResponseEntity<ViewStatsDto[]> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
		String startStr = FORMATTER.format(start);
		String endStr = FORMATTER.format(end);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/stats")
				.queryParam("start", startStr)
				.queryParam("end", endStr)
				.queryParam("unique", unique);
		if (uris != null && !uris.isEmpty()) {
			for (String uri : uris) {
				builder.queryParam("uris", uri);
			}
		}
		return restTemplate.getForEntity(builder.build().encode().toUri(), ViewStatsDto[].class);
	}
}


