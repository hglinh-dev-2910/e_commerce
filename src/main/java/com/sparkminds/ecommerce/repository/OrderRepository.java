package com.sparkminds.ecommerce.repository;

import com.sparkminds.ecommerce.entity.Order;
import com.sparkminds.ecommerce.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Page<Order> findByUser(User user, Pageable pageable);

    Optional<Order> findByIdAndUser(Long id, User user);
}
