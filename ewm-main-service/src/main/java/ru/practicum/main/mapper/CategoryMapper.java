package ru.practicum.main.mapper;

import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.model.Category;

public final class CategoryMapper {
	private CategoryMapper() {
	}

	public static CategoryDto toCategoryDto(Category category) {
		CategoryDto dto = new CategoryDto();
		dto.setId(category.getId());
		dto.setName(category.getName());
		return dto;
	}

	public static Category toCategory(ru.practicum.main.dto.NewCategoryDto dto) {
		Category category = new Category();
		category.setName(dto.getName());
		return category;
	}
}
