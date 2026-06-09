package com.sparkminds.ecommerce.service;

import com.sparkminds.ecommerce.dto.request.OrderRequest;
import com.sparkminds.ecommerce.dto.request.OrderStatusUpdateRequest;
import com.sparkminds.ecommerce.dto.response.OrderResponse;
import com.sparkminds.ecommerce.dto.response.PagedResponse;

public interface OrderService {

    OrderResponse placeOrder(OrderRequest request, String username);

    OrderResponse getOrderById(Long id, String username, boolean isAdmin);

    PagedResponse<OrderResponse> getMyOrders(String username, int page, int size);

    PagedResponse<OrderResponse> getAllOrders(String status, int page, int size);

    OrderResponse cancelOrder(Long id, String username, boolean isAdmin);

    OrderResponse updateOrderStatus(Long id, OrderStatusUpdateRequest request);
}
