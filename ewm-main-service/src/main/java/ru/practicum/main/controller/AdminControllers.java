package ru.practicum.main.controller;

import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.dto.CompilationDto;
import ru.practicum.main.dto.EventFullDto;
import ru.practicum.main.dto.NewCategoryDto;
import ru.practicum.main.dto.NewCompilationDto;
import ru.practicum.main.dto.NewUserRequest;
import ru.practicum.main.dto.UpdateCompilationRequest;
import ru.practicum.main.dto.UpdateEventAdminRequest;
import ru.practicum.main.dto.UserDto;

@RestController
@RequestMapping("/admin")
public class AdminControllers {

	@PostMapping("/categories")
	public ResponseEntity<CategoryDto> addCategory(@RequestBody NewCategoryDto newCategoryDto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(new CategoryDto());
	}

	@DeleteMapping("/categories/{catId}")
	public ResponseEntity<Void> deleteCategory(@PathVariable("catId") Long catId) {
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/categories/{catId}")
	public ResponseEntity<CategoryDto> updateCategory(@PathVariable("catId") Long catId,
	                                                  @RequestBody CategoryDto categoryDto) {
		return ResponseEntity.ok(new CategoryDto());
	}

	@PostMapping("/compilations")
	public ResponseEntity<CompilationDto> saveCompilation(@RequestBody NewCompilationDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(new CompilationDto());
	}

	@DeleteMapping("/compilations/{compId}")
	public ResponseEntity<Void> deleteCompilation(@PathVariable("compId") Long compId) {
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/compilations/{compId}")
	public ResponseEntity<CompilationDto> updateCompilation(@PathVariable("compId") Long compId,
	                                                        @RequestBody UpdateCompilationRequest dto) {
		return ResponseEntity.ok(new CompilationDto());
	}

	@GetMapping("/events")
	public List<EventFullDto> getEvents(@RequestParam(value = "users", required = false) List<Long> users,
	                                    @RequestParam(value = "states", required = false) List<String> states,
	                                    @RequestParam(value = "categories", required = false) List<Long> categories,
	                                    @RequestParam(value = "rangeStart", required = false) String rangeStart,
	                                    @RequestParam(value = "rangeEnd", required = false) String rangeEnd,
	                                    @RequestParam(value = "from", defaultValue = "0") Integer from,
	                                    @RequestParam(value = "size", defaultValue = "10") Integer size) {
		return Collections.emptyList();
	}

	@PatchMapping("/events/{eventId}")
	public ResponseEntity<EventFullDto> updateEvent(@PathVariable("eventId") Long eventId,
	                                                @RequestBody UpdateEventAdminRequest dto) {
		return ResponseEntity.ok(new EventFullDto());
	}

	@GetMapping("/users")
	public List<UserDto> getUsers(@RequestParam(value = "ids", required = false) List<Long> ids,
	                              @RequestParam(value = "from", defaultValue = "0") Integer from,
	                              @RequestParam(value = "size", defaultValue = "10") Integer size) {
		return Collections.emptyList();
	}

	@PostMapping("/users")
	public ResponseEntity<UserDto> registerUser(@RequestBody NewUserRequest dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(new UserDto());
	}

	@DeleteMapping("/users/{userId}")
	public ResponseEntity<Void> deleteUser(@PathVariable("userId") Long userId) {
		return ResponseEntity.noContent().build();
	}
}


