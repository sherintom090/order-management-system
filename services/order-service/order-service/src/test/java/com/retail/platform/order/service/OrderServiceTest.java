package com.retail.platform.order.service;

import com.retail.platform.order.dto.CreateOrderRequest;
import com.retail.platform.order.dto.OrderResponse;
import com.retail.platform.order.entity.Order;
import com.retail.platform.order.exception.OrderNotFoundException;
import com.retail.platform.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldGetOrdersWithPagination() {

        Order order1 = new Order();

        order1.setId(1L);
        order1.setCustomerId("CUST-1001");
        order1.setStatus("CREATED");
        order1.setTotalAmount(new BigDecimal("149.99"));
        order1.setCurrency("USD");
        order1.setCreatedAt(OffsetDateTime.now());
        order1.setUpdatedAt(OffsetDateTime.now());

        Order order2 = new Order();

        order2.setId(2L);
        order2.setCustomerId("CUST-1002");
        order2.setStatus("CREATED");
        order2.setTotalAmount(new BigDecimal("249.99"));
        order2.setCurrency("USD");
        order2.setCreatedAt(OffsetDateTime.now());
        order2.setUpdatedAt(OffsetDateTime.now());

        Pageable pageable = PageRequest.of(0, 2);

        Page<Order> orderPage =
                new PageImpl<>(
                        List.of(order1, order2),
                        pageable,
                        5
                );

        when(orderRepository.findAll(pageable))
                .thenReturn(orderPage);

        Page<OrderResponse> response =
                orderService.getOrders("CREATED",pageable);

        assertNotNull(response);

        assertEquals(2, response.getContent().size());
        assertEquals(5, response.getTotalElements());
        assertEquals(3, response.getTotalPages());
        assertEquals(0, response.getNumber());
        assertEquals(2, response.getSize());

        assertEquals(1L, response.getContent().get(0).id());
        assertEquals("CUST-1001",
                response.getContent().get(0).customerId());

        assertEquals(2L, response.getContent().get(1).id());
        assertEquals("CUST-1002",
                response.getContent().get(1).customerId());

        verify(orderRepository).findAll(pageable);
    }

}

