package ru.practicum.main.controller;

import java.util.Collections;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.dto.CompilationDto;
import ru.practicum.main.dto.EventFullDto;
import ru.practicum.main.dto.EventShortDto;

@RestController
@RequestMapping
public class PublicControllers {

	@GetMapping("/categories")
	public List<CategoryDto> getCategories(@RequestParam(value = "from", defaultValue = "0") Integer from,
	                                       @RequestParam(value = "size", defaultValue = "10") Integer size) {
		return Collections.emptyList();
	}

	@GetMapping("/categories/{catId}")
	public ResponseEntity<CategoryDto> getCategory(@PathVariable("catId") Long catId) {
		return ResponseEntity.ok(new CategoryDto());
	}

	@GetMapping("/compilations")
	public List<CompilationDto> getCompilations(@RequestParam(value = "pinned", required = false) Boolean pinned,
	                                            @RequestParam(value = "from", defaultValue = "0") Integer from,
	                                            @RequestParam(value = "size", defaultValue = "10") Integer size) {
		return Collections.emptyList();
	}

	@GetMapping("/compilations/{compId}")
	public ResponseEntity<CompilationDto> getCompilation(@PathVariable("compId") Long compId) {
		return ResponseEntity.ok(new CompilationDto());
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
		return Collections.emptyList();
	}

	@GetMapping("/events/{id}")
	public ResponseEntity<EventFullDto> getEvent(@PathVariable("id") Long id) {
		return ResponseEntity.ok(new EventFullDto());
	}
}


