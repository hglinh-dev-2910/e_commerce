package com.sparkminds.ecommerce.service;

import com.sparkminds.ecommerce.dto.request.ProductRequest;
import com.sparkminds.ecommerce.dto.response.PagedResponse;
import com.sparkminds.ecommerce.dto.response.ProductResponse;

import java.math.BigDecimal;

public interface ProductService {

    PagedResponse<ProductResponse> getAllProducts(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sort,
            int page,
            int size
    );

    ProductResponse getProductById(Long id);

    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);
}
