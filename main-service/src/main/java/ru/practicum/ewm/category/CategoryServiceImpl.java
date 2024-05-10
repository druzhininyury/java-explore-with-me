package ru.practicum.ewm.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.mapping.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.exception.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        Category category = categoryRepository.save(categoryMapper.toCategory(newCategoryDto));
        log.info("Added category: {}", category);
        return categoryMapper.toCategoryDto(category);
    }

    @Transactional
    public void deleteCategory(long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new EntityNotFoundException("Category with id=" + categoryId + " was not found");
        }
        log.info("Deleted category with id={}", categoryId);
        categoryRepository.deleteById(categoryId);
    }

    @Transactional
    public CategoryDto updateCategory(long categoryId, NewCategoryDto newCategoryDto) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new EntityNotFoundException("Category with id=" + categoryId + " was not found.");
        }
        Category category = categoryRepository.save(categoryMapper.toCategory(newCategoryDto, categoryId));
        log.info("Updated category: {}", category);
        return categoryMapper.toCategoryDto(category);
    }

    public List<CategoryDto> getAllCategories(int from, int size) {
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));
        List<Category> categories = categoryRepository.findAll(pageRequest).getContent();
        log.info("Sent all categories with id from={} and size={}", from, size);
        return categoryMapper.toCategoryDto(categories);
    }

    public CategoryDto getCategory(long categoryId) {
        Optional<Category> result = categoryRepository.findById(categoryId);
        if (result.isEmpty()) {
            throw new EntityNotFoundException("Category with id=" + categoryId + " was not found");
        }
        log.info("Response for get category: " + result.get());
        return categoryMapper.toCategoryDto(result.get());
    }

}
