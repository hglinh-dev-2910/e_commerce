package com.sparkminds.ecommerce.repository;

import com.sparkminds.ecommerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByIsActiveTrue();

    boolean existsByNameIgnoreCase(String name);
}
