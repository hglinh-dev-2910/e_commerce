package com.sparkminds.ecommerce.service.impl;

import com.sparkminds.ecommerce.dto.request.OrderItemRequest;
import com.sparkminds.ecommerce.dto.request.OrderRequest;
import com.sparkminds.ecommerce.dto.request.OrderStatusUpdateRequest;
import com.sparkminds.ecommerce.dto.response.OrderResponse;
import com.sparkminds.ecommerce.dto.response.PagedResponse;
import com.sparkminds.ecommerce.entity.*;
import com.sparkminds.ecommerce.exception.BadRequestException;
import com.sparkminds.ecommerce.exception.ResourceNotFoundException;
import com.sparkminds.ecommerce.repository.OrderRepository;
import com.sparkminds.ecommerce.repository.ProductRepository;
import com.sparkminds.ecommerce.repository.UserRepository;
import com.sparkminds.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OrderResponse placeOrder(OrderRequest request, String username) {
        User user = getUserByUsername(username);

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .note(request.getNote())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findByIdAndIsActiveTrue(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemReq.getProductId()));

            if (product.getStockQuantity() < itemReq.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();

            order.addOrderItem(orderItem);

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        order.setTotalAmount(totalAmount);
        orderRepository.save(order);

        log.info("[ORDERS] Placed order {}", order.getId());

        return OrderResponse.fromEntity(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id, String username, boolean isAdmin) {
        Order order = getOrderAndCheckAccess(id, username, isAdmin);
        return OrderResponse.fromEntity(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getMyOrders(String username, int page, int size) {
        User user = getUserByUsername(username);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = orderRepository.findByUser(user, pageable);

        List<OrderResponse> content = orderPage.getContent()
                .stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());

        log.info("[ORDERS] Retrieved orders for user {}", username);

        return PagedResponse.from(orderPage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getAllOrders(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<Order> spec = Specification.where((Specification<Order>) null);

        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb) -> {
                try {
                    OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                    return cb.equal(root.get("status"), orderStatus);
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException("Invalid order status: " + status);
                }
            });
        }

        Page<Order> orderPage = orderRepository.findAll(spec, pageable);

        List<OrderResponse> content = orderPage.getContent()
                .stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());

        return PagedResponse.from(orderPage, content);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id, String username, boolean isAdmin) {
        Order order = getOrderAndCheckAccess(id, username, isAdmin);

        if (order.getStatus() != OrderStatus.PENDING && !isAdmin) {
            throw new BadRequestException("Only PENDING orders can be cancelled by user");
        }

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order cannot be cancelled in its current state");
        }

        // Restore stock
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        log.info("[ORDERS] Cancelled order {}", order.getId());

        return OrderResponse.fromEntity(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = request.getStatus();

        if (currentStatus == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot update status of a CANCELLED order");
        }

        if (newStatus == OrderStatus.CANCELLED) {
            return cancelOrder(id, null, true); // Admin cancelling
        }

        boolean validTransition = false;
        if (currentStatus == OrderStatus.PENDING && newStatus == OrderStatus.CONFIRMED) validTransition = true;
        if (currentStatus == OrderStatus.CONFIRMED && newStatus == OrderStatus.SHIPPED) validTransition = true;
        if (currentStatus == OrderStatus.SHIPPED && newStatus == OrderStatus.DELIVERED) validTransition = true;

        if (!validTransition) {
            throw new BadRequestException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        log.info("[ORDERS] Updated order {}", order.getId());

        return OrderResponse.fromEntity(order);
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private Order getOrderAndCheckAccess(Long id, String username, boolean isAdmin) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        if (!isAdmin && !order.getUser().getUsername().equals(username)) {
            throw new BadRequestException("You don't have permission to access this order");
        }

        return order;
    }
}
