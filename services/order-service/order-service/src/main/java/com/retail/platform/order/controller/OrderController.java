package com.retail.platform.order.controller;

import com.retail.platform.order.dto.CreateOrderRequest;
import com.retail.platform.order.dto.OrderResponse;
import com.retail.platform.order.entity.Order;
import com.retail.platform.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateOrderRequest request) {

        Order order = new Order();

        order.setCustomerId(request.customerId());
        order.setTotalAmount(request.totalAmount());
        order.setCurrency(request.currency());
        order.setStatus("CREATED");

        Order savedOrder = orderService.createOrder(idempotencyKey, order);

        return new OrderResponse(
                savedOrder.getId(),
                savedOrder.getCustomerId(),
                savedOrder.getStatus(),
                savedOrder.getTotalAmount(),
                savedOrder.getCurrency(),
                savedOrder.getCreatedAt(),
                savedOrder.getUpdatedAt()
        );
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {

        Order order = orderService.getOrder(id);

        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    @GetMapping
    public Page<OrderResponse> getOrders(@RequestParam(required = false) String status, Pageable pageable) {
        return orderService.getOrders(status, pageable);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
    }

}