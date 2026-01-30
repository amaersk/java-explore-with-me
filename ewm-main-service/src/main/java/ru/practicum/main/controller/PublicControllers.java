package ru.practicum.main.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.dto.CompilationDto;
import ru.practicum.main.dto.CommentDto;
import ru.practicum.main.dto.EventFullDto;
import ru.practicum.main.dto.EventShortDto;
import ru.practicum.main.service.CategoryService;
import ru.practicum.main.service.CommentService;
import ru.practicum.main.service.CompilationService;
import ru.practicum.main.service.EventService;

@RestController
@RequestMapping
public class PublicControllers {
	private final CategoryService categoryService;
	private final CompilationService compilationService;
	private final EventService eventService;
	private final CommentService commentService;

	public PublicControllers(CategoryService categoryService, CompilationService compilationService,
	                         EventService eventService, CommentService commentService) {
		this.categoryService = categoryService;
		this.compilationService = compilationService;
		this.eventService = eventService;
		this.commentService = commentService;
	}

	@GetMapping("/categories")
	public List<CategoryDto> getCategories(@RequestParam(value = "from", defaultValue = "0") Integer from,
	                                       @RequestParam(value = "size", defaultValue = "10") Integer size) {
		return categoryService.getCategories(from, size);
	}

	@GetMapping("/categories/{catId}")
	public ResponseEntity<CategoryDto> getCategory(@PathVariable("catId") Long catId) {
		return ResponseEntity.ok(categoryService.getCategory(catId));
	}

	@GetMapping("/compilations")
	public List<CompilationDto> getCompilations(@RequestParam(value = "pinned", required = false) Boolean pinned,
	                                            @RequestParam(value = "from", defaultValue = "0") Integer from,
	                                            @RequestParam(value = "size", defaultValue = "10") Integer size) {
		return compilationService.getCompilations(pinned, from, size);
	}

	@GetMapping("/compilations/{compId}")
	public ResponseEntity<CompilationDto> getCompilation(@PathVariable("compId") Long compId) {
		return ResponseEntity.ok(compilationService.getCompilation(compId));
	}

	@GetMapping("/events")
	public List<EventShortDto> getEvents(@RequestParam(value = "text", required = false) String text,
	                                     @RequestParam(value = "categories", required = false) List<Long> categories,
	                                     @RequestParam(value = "paid", required = false) Boolean paid,
	                                     @RequestParam(value = "rangeStart", required = false) String rangeStart,
	                                     @RequestParam(value = "rangeEnd", required = false) String rangeEnd,
	                                     @RequestParam(value = "onlyAvailable", defaultValue = "false") Boolean onlyAvailable,
	                                     @RequestParam(value = "sort", required = false) String sort,
	                                     @RequestParam(value = "from", defaultValue = "0") Integer from,
	                                     @RequestParam(value = "size", defaultValue = "10") Integer size) {
		return eventService.getPublicEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
	}

	@GetMapping("/events/{id}")
	public ResponseEntity<EventFullDto> getEvent(@PathVariable("id") Long id) {
		return ResponseEntity.ok(eventService.getPublicEvent(id));
	}

	@GetMapping("/events/{eventId}/comments")
	public List<CommentDto> getEventComments(@PathVariable("eventId") Long eventId,
	                                         @RequestParam(value = "from", defaultValue = "0") Integer from,
	                                         @RequestParam(value = "size", defaultValue = "10") Integer size) {
		return commentService.getPublicComments(eventId, from, size);
	}
}


