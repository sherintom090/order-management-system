package com.retail.platform.order.service;

import com.retail.platform.order.dto.OrderResponse;
import com.retail.platform.order.entity.IdempotencyKeyEntity;
import com.retail.platform.order.entity.Order;
import com.retail.platform.order.event.OrderCreatedEvent;
import com.retail.platform.order.event.OrderCancelledEvent;
import com.retail.platform.order.event.OrderDeletedEvent;
import com.retail.platform.order.exception.OrderNotFoundException;
import com.retail.platform.order.kafka.OrderEventProducer;
import com.retail.platform.order.repository.IdempotencyKeyRepository;
import com.retail.platform.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.platform.order.outbox.OutboxEvent;
import com.retail.platform.order.outbox.OutboxEventRepository;


import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final OrderEventProducer orderEventProducer;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;


    @Transactional
    public Order createOrder(String idempotencyKey,Order order) {

        Optional<IdempotencyKeyEntity> existing =
                idempotencyKeyRepository.findByIdempotencyKey(idempotencyKey);

        if (existing.isPresent()) {
            Order existingOrder = orderRepository
                    .findById(existing.get().getOrderId())
                    .orElseThrow(() -> new OrderNotFoundException(
                            existing.get().getOrderId()));

            return existingOrder;
        }

        Order savedOrder = orderRepository.save(order);
        IdempotencyKeyEntity keyEntity = new IdempotencyKeyEntity();

        keyEntity.setIdempotencyKey(idempotencyKey);
        keyEntity.setOrderId(savedOrder.getId());
        keyEntity.setCreatedAt(OffsetDateTime.now());

        idempotencyKeyRepository.save(keyEntity);

        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(),
                savedOrder.getId(),
                savedOrder.getCustomerId(),
                savedOrder.getTotalAmount(),
                savedOrder.getCurrency(),
                savedOrder.getStatus(),
                savedOrder.getCreatedAt()
        );

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateType("ORDER");
        outboxEvent.setAggregateId(savedOrder.getId().toString());
        outboxEvent.setEventType("ORDER_CREATED");
        outboxEvent.setPayload(toJson(event));
        outboxEvent.setStatus("PENDING");

        outboxEventRepository.save(outboxEvent);


        return savedOrder;
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(
            String status,
            Pageable pageable) {

        Page<Order> orders;

        if (status != null && !status.isBlank()) {
            orders = orderRepository.findByStatus(status, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }

        return orders.map(this::toResponse);
    }

    private OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    @Transactional
    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        order.setStatus("CANCELLED");
        Order saved = orderRepository.save(order);

        OrderCancelledEvent event = new OrderCancelledEvent(
                UUID.randomUUID(),
                saved.getId(),
                saved.getCustomerId(),
                saved.getStatus(),
                saved.getUpdatedAt()
        );

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateType("ORDER");
        outboxEvent.setAggregateId(saved.getId().toString());
        outboxEvent.setEventType("ORDER_CANCELLED");
        outboxEvent.setPayload(toJson(event));
        outboxEvent.setStatus("PENDING");

        outboxEventRepository.save(outboxEvent);
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        orderRepository.deleteById(id);

        OrderDeletedEvent event = new OrderDeletedEvent(
                UUID.randomUUID(),
                id
        );

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateType("ORDER");
        outboxEvent.setAggregateId(String.valueOf(id));
        outboxEvent.setEventType("ORDER_DELETED");
        outboxEvent.setPayload(toJson(event));
        outboxEvent.setStatus("PENDING");

        outboxEventRepository.save(outboxEvent);
    }

    private String toJson(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize event",
                    e
            );
        }
    }


}