package com.sparkminds.ecommerce.service;

import com.sparkminds.ecommerce.dto.request.CategoryRequest;
import com.sparkminds.ecommerce.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getAllActiveCategories();

    CategoryResponse createCategory(CategoryRequest request);

    void deleteCategory(Long id);
}
