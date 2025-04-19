package ru.practicum.service.category;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.category.CategoryRepository;
import ru.practicum.repository.event.EventRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createCategory_shouldCreateNewCategory() {
        NewCategoryDto newCategoryDto = new NewCategoryDto("New Category");
        Category category = new Category(1L, "New Category");
        CategoryDto expectedDto = new CategoryDto(1L, "New Category");

        when(categoryRepository.existsByName(newCategoryDto.getName())).thenReturn(false);
        when(categoryMapper.toCategory(newCategoryDto)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toCategoryDto(category)).thenReturn(expectedDto);

        CategoryDto result = categoryService.createCategory(newCategoryDto);

        assertEquals(expectedDto, result);
        verify(categoryRepository).save(category);
    }

    @Test
    void createCategory_shouldThrowConflictExceptionWhenNameExists() {
        NewCategoryDto newCategoryDto = new NewCategoryDto("Existing Category");

        when(categoryRepository.existsByName(newCategoryDto.getName())).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoryService.createCategory(newCategoryDto));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void deleteCategory_shouldDeleteCategoryWhenNoEvents() {
        Category category = new Category(1L, "Category to delete");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.existsByCategoryId(1L)).thenReturn(false);

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_shouldThrowNotFoundExceptionWhenCategoryNotFound() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.deleteCategory(1L));
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void deleteCategory_shouldThrowConflictExceptionWhenCategoryHasEvents() {
        Category category = new Category(1L, "Category with events");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoryService.deleteCategory(1L));
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void updateCategory_shouldUpdateCategoryName() {
        NewCategoryDto updateDto = new NewCategoryDto("Updated Name");
        Category existingCategory = new Category(1L, "Original Name");
        Category updatedCategory = new Category(1L, "Updated Name");
        CategoryDto expectedDto = new CategoryDto(1L, "Updated Name");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsByNameAndIdNot("Updated Name", 1L)).thenReturn(false);
        when(categoryRepository.save(any())).thenReturn(updatedCategory);
        when(categoryMapper.toCategoryDto(updatedCategory)).thenReturn(expectedDto);

        CategoryDto result = categoryService.updateCategory(1L, updateDto);

        assertEquals(expectedDto, result);
        assertEquals("Updated Name", updatedCategory.getName());
    }

    @Test
    void updateCategory_shouldThrowConflictExceptionWhenNewNameExists() {
        NewCategoryDto updateDto = new NewCategoryDto("Existing Name");
        Category existingCategory = new Category(1L, "Original Name");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsByNameAndIdNot("Existing Name", 1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoryService.updateCategory(1L, updateDto));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_shouldThrowNotFoundExceptionWhenCategoryNotFound() {
        NewCategoryDto updateDto = new NewCategoryDto("New Name");

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.updateCategory(1L, updateDto));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getCategories_shouldReturnPageOfCategories() {
        Pageable pageable = PageRequest.of(0, 10);
        Category category1 = new Category(1L, "Category 1");
        Category category2 = new Category(2L, "Category 2");
        Page<Category> page = new PageImpl<>(List.of(category1, category2));
        CategoryDto dto1 = new CategoryDto(1L, "Category 1");
        CategoryDto dto2 = new CategoryDto(2L, "Category 2");

        when(categoryRepository.findAll(pageable)).thenReturn(page);
        when(categoryMapper.toCategoryDto(category1)).thenReturn(dto1);
        when(categoryMapper.toCategoryDto(category2)).thenReturn(dto2);

        List<CategoryDto> result = categoryService.getCategories(0, 10);

        assertEquals(2, result.size());
        assertEquals(dto1, result.get(0));
        assertEquals(dto2, result.get(1));
    }

    @Test
    void getCategoryById_shouldReturnCategory() {
        Category category = new Category(1L, "Test Category");
        CategoryDto expectedDto = new CategoryDto(1L, "Test Category");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.toCategoryDto(category)).thenReturn(expectedDto);

        CategoryDto result = categoryService.getCategoryById(1L);

        assertEquals(expectedDto, result);
    }

    @Test
    void getCategoryById_shouldThrowNotFoundExceptionWhenCategoryNotFound() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.getCategoryById(1L));
    }
}