package com.sparkminds.ecommerce.service.impl;

import com.sparkminds.ecommerce.dto.request.CategoryRequest;
import com.sparkminds.ecommerce.dto.response.CategoryResponse;
import com.sparkminds.ecommerce.entity.Category;
import com.sparkminds.ecommerce.exception.DuplicateResourceException;
import com.sparkminds.ecommerce.exception.ResourceNotFoundException;
import com.sparkminds.ecommerce.repository.CategoryRepository;
import com.sparkminds.ecommerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponse> getAllActiveCategories() {

        log.info("[CATEGORIES] Getting all categories");
        return categoryRepository.findAllByIsActiveTrue()
                .stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());

    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Category name already exists: " + request.getName());
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        categoryRepository.save(category);
        log.info("[CATEGORIES] Created category {}", category.getName());
        return CategoryResponse.fromEntity(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        category.setActive(false);
        categoryRepository.save(category);

        log.info("[CATEGORIES] Deleted category {}", category.getName());
    }
}
