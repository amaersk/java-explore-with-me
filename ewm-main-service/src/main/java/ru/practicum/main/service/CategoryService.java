package ru.practicum.main.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.mapper.CategoryMapper;
import ru.practicum.main.model.Category;
import ru.practicum.main.repository.CategoryRepository;
import ru.practicum.main.repository.EventRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CategoryService {
	private final CategoryRepository categoryRepository;
	private final EventRepository eventRepository;
	private final CategoryMapper categoryMapper;

	public CategoryService(CategoryRepository categoryRepository, EventRepository eventRepository, CategoryMapper categoryMapper) {
		this.categoryRepository = categoryRepository;
		this.eventRepository = eventRepository;
		this.categoryMapper = categoryMapper;
	}

	public List<CategoryDto> getCategories(Integer from, Integer size) {
		if (size <= 0) {
			throw new BadRequestException("Size must be greater than 0");
		}
		Pageable pageable = PageRequest.of(from / size, size);
		Page<Category> categories = categoryRepository.findAll(pageable);
		return categories.getContent().stream()
				.map(categoryMapper::toCategoryDto)
				.collect(Collectors.toList());
	}

	public CategoryDto getCategory(Long catId) {
		Category category = findCategoryById(catId);
		return categoryMapper.toCategoryDto(category);
	}

	@Transactional
	public CategoryDto createCategory(ru.practicum.main.dto.NewCategoryDto dto) {
		if (categoryRepository.existsByName(dto.getName())) {
			throw new ConflictException("Category name must be unique");
		}
		Category category = categoryMapper.toCategory(dto);
		Category saved = categoryRepository.save(category);
		return categoryMapper.toCategoryDto(saved);
	}

	@Transactional
	public CategoryDto updateCategory(Long catId, CategoryDto dto) {
		Category category = findCategoryById(catId);
		String newName = dto.getName();
		if (!category.getName().equals(newName) && categoryRepository.existsByName(newName)) {
			throw new ConflictException("Category name must be unique");
		}
		category.setName(newName);
		Category saved = categoryRepository.save(category);
		return categoryMapper.toCategoryDto(saved);
	}

	@Transactional
	public void deleteCategory(Long catId) {
		Category category = findCategoryById(catId);
		if (eventRepository.existsByCategoryId(catId)) {
			throw new ConflictException("The category is not empty");
		}
		categoryRepository.delete(category);
	}

	public Category findCategoryById(Long catId) {
		return categoryRepository.findById(catId)
				.orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));
	}
}
