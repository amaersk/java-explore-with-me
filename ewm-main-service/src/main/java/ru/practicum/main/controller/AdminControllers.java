package ru.practicum.main.controller;

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
import jakarta.validation.Valid;
import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.dto.CompilationDto;
import ru.practicum.main.dto.EventFullDto;
import ru.practicum.main.dto.NewCategoryDto;
import ru.practicum.main.dto.NewCompilationDto;
import ru.practicum.main.dto.NewUserRequest;
import ru.practicum.main.dto.UpdateCompilationRequest;
import ru.practicum.main.dto.UpdateEventAdminRequest;
import ru.practicum.main.dto.UserDto;
import ru.practicum.main.service.CategoryService;
import ru.practicum.main.service.CompilationService;
import ru.practicum.main.service.EventService;
import ru.practicum.main.service.UserService;

@RestController
@RequestMapping("/admin")
public class AdminControllers {
	private final UserService userService;
	private final CategoryService categoryService;
	private final EventService eventService;
	private final CompilationService compilationService;

	public AdminControllers(UserService userService, CategoryService categoryService,
	                       EventService eventService, CompilationService compilationService) {
		this.userService = userService;
		this.categoryService = categoryService;
		this.eventService = eventService;
		this.compilationService = compilationService;
	}

	@PostMapping("/categories")
	public ResponseEntity<CategoryDto> addCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(newCategoryDto));
	}

	@DeleteMapping("/categories/{catId}")
	public ResponseEntity<Void> deleteCategory(@PathVariable("catId") Long catId) {
		categoryService.deleteCategory(catId);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/categories/{catId}")
	public ResponseEntity<CategoryDto> updateCategory(@PathVariable("catId") Long catId,
	                                                  @Valid @RequestBody CategoryDto categoryDto) {
		return ResponseEntity.ok(categoryService.updateCategory(catId, categoryDto));
	}

	@PostMapping("/compilations")
	public ResponseEntity<CompilationDto> saveCompilation(@Valid @RequestBody NewCompilationDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(compilationService.createCompilation(dto));
	}

	@DeleteMapping("/compilations/{compId}")
	public ResponseEntity<Void> deleteCompilation(@PathVariable("compId") Long compId) {
		compilationService.deleteCompilation(compId);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/compilations/{compId}")
	public ResponseEntity<CompilationDto> updateCompilation(@PathVariable("compId") Long compId,
	                                                        @Valid @RequestBody UpdateCompilationRequest dto) {
		return ResponseEntity.ok(compilationService.updateCompilation(compId, dto));
	}

	@GetMapping("/events")
	public List<EventFullDto> getEvents(@RequestParam(value = "users", required = false) List<Long> users,
	                                    @RequestParam(value = "states", required = false) List<String> states,
	                                    @RequestParam(value = "categories", required = false) List<Long> categories,
	                                    @RequestParam(value = "rangeStart", required = false) String rangeStart,
	                                    @RequestParam(value = "rangeEnd", required = false) String rangeEnd,
	                                    @RequestParam(value = "from", defaultValue = "0") Integer from,
	                                    @RequestParam(value = "size", defaultValue = "10") Integer size) {
		return eventService.getAdminEvents(users, states, categories, rangeStart, rangeEnd, from, size);
	}

	@PatchMapping("/events/{eventId}")
	public ResponseEntity<EventFullDto> updateEvent(@PathVariable("eventId") Long eventId,
	                                                @Valid @RequestBody UpdateEventAdminRequest dto) {
		return ResponseEntity.ok(eventService.updateEventByAdmin(eventId, dto));
	}

	@GetMapping("/users")
	public List<UserDto> getUsers(@RequestParam(value = "ids", required = false) List<Long> ids,
	                              @RequestParam(value = "from", defaultValue = "0") Integer from,
	                              @RequestParam(value = "size", defaultValue = "10") Integer size) {
		return userService.getUsers(ids, from, size);
	}

	@PostMapping("/users")
	public ResponseEntity<UserDto> registerUser(@Valid @RequestBody NewUserRequest dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(dto));
	}

	@DeleteMapping("/users/{userId}")
	public ResponseEntity<Void> deleteUser(@PathVariable("userId") Long userId) {
		userService.deleteUser(userId);
		return ResponseEntity.noContent().build();
	}
}


