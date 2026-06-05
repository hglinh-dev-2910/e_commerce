package com.sparkminds.ecommerce.repository;

import com.sparkminds.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    Optional<Product> findByIdAndIsActiveTrue(Long id);

    boolean existsByCategoryIdAndIsActiveTrue(Long categoryId);
}
