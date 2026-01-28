package ru.practicum.main.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ErrorHandler {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
		List<String> errors = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> {
					String field = error.getField();
					String message = error.getDefaultMessage();
					Object rejectedValue = error.getRejectedValue();
					return String.format("Field: %s. Error: %s. Value: %s", field, message, rejectedValue);
				})
				.collect(Collectors.toList());

		String errorMessage = errors.isEmpty() ? "Validation failed" : errors.get(0);

		ErrorResponse errorResponse = new ErrorResponse(
				"400 BAD_REQUEST",
				"Incorrectly made request.",
				errorMessage,
				LocalDateTime.now().format(FORMATTER),
				errors
		);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
		ErrorResponse errorResponse = new ErrorResponse(
				"404 NOT_FOUND",
				"The required object was not found.",
				ex.getMessage(),
				LocalDateTime.now().format(FORMATTER),
				new ArrayList<>()
		);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
	public ResponseEntity<ErrorResponse> handleRequestParameterExceptions(Exception ex) {
		ErrorResponse errorResponse = new ErrorResponse(
				"400 BAD_REQUEST",
				"Incorrectly made request.",
				ex.getMessage(),
				LocalDateTime.now().format(FORMATTER),
				new ArrayList<>()
		);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ErrorResponse> handleConflictException(ConflictException ex) {
		ErrorResponse errorResponse = new ErrorResponse(
				"409 CONFLICT",
				"For the requested operation the conditions are not met.",
				ex.getMessage(),
				LocalDateTime.now().format(FORMATTER),
				new ArrayList<>()
		);
		return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
		ErrorResponse errorResponse = new ErrorResponse(
				"400 BAD_REQUEST",
				"Incorrectly made request.",
				ex.getMessage(),
				LocalDateTime.now().format(FORMATTER),
				new ArrayList<>()
		);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception ex) {
		ErrorResponse errorResponse = new ErrorResponse(
				"500 INTERNAL_SERVER_ERROR",
				"Internal server error.",
				ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred",
				LocalDateTime.now().format(FORMATTER),
				new ArrayList<>()
		);
		ex.printStackTrace();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}

	public static class ErrorResponse {
		private String status;
		private String reason;
		private String message;
		private String timestamp;
		private List<String> errors;

		public ErrorResponse(String status, String reason, String message, String timestamp, List<String> errors) {
			this.status = status;
			this.reason = reason;
			this.message = message;
			this.timestamp = timestamp;
			this.errors = errors != null ? errors : new ArrayList<>();
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getReason() {
			return reason;
		}

		public void setReason(String reason) {
			this.reason = reason;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(String timestamp) {
			this.timestamp = timestamp;
		}

		public List<String> getErrors() {
			return errors;
		}

		public void setErrors(List<String> errors) {
			this.errors = errors;
		}
	}
}
