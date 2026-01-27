package ru.practicum.main.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.practicum.stats.client.StatsClient;

@Component
@Order(1)
public class StatsFilter extends OncePerRequestFilter {
	private static final String APP_NAME = "ewm-main-service";
	private final StatsClient statsClient;

	@Autowired
	public StatsFilter(StatsClient statsClient) {
		this.statsClient = statsClient;
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, 
	                                @NonNull HttpServletResponse response, 
	                                @NonNull FilterChain filterChain)
			throws ServletException, IOException {
		filterChain.doFilter(request, response);
		
		if (shouldTrack(request)) {
			String uri = request.getRequestURI();
			String ip = getClientIp(request);
			LocalDateTime timestamp = LocalDateTime.now();
			
			CompletableFuture.runAsync(() -> {
				try {
					statsClient.sendHit(APP_NAME, uri, ip, timestamp);
				} catch (Exception e) {
					logger.error("Failed to send statistics", e);
				}
			});
		}
	}

	private boolean shouldTrack(HttpServletRequest request) {
		String method = request.getMethod();
		String path = request.getRequestURI();
		
		return "GET".equals(method) && 
			   (path.startsWith("/events") || 
			    path.startsWith("/compilations") || 
			    path.startsWith("/categories"));
	}

	private String getClientIp(HttpServletRequest request) {
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
			return xForwardedFor.split(",")[0].trim();
		}
		
		String xRealIp = request.getHeader("X-Real-IP");
		if (xRealIp != null && !xRealIp.isEmpty()) {
			return xRealIp;
		}
		
		return request.getRemoteAddr();
	}
}
