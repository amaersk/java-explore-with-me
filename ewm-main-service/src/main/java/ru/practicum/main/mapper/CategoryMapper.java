package ru.practicum.main.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.model.Category;

@Component
public class CategoryMapper {
	public CategoryDto toCategoryDto(Category category) {
		CategoryDto dto = new CategoryDto();
		dto.setId(category.getId());
		dto.setName(category.getName());
		return dto;
	}

	public Category toCategory(ru.practicum.main.dto.NewCategoryDto dto) {
		Category category = new Category();
		category.setName(dto.getName());
		return category;
	}
}
