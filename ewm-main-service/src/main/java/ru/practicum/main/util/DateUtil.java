package ru.practicum.main.util;

import java.time.format.DateTimeFormatter;

public final class DateUtil {
	public static final DateTimeFormatter MAIN_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	public static final DateTimeFormatter REQUEST_CREATED_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

	private DateUtil() {
	}
}

