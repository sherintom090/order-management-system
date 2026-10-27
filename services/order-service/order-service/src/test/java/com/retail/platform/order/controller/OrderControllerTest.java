package com.retail.platform.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.platform.order.dto.CreateOrderRequest;
import com.retail.platform.order.entity.Order;
import com.retail.platform.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    void shouldCreateOrder() throws Exception {

        Order order = new Order();
        order.setId(10L);
        order.setCustomerId("CUST-1001");
        order.setStatus("CREATED");
        order.setTotalAmount(new BigDecimal("149.99"));
        order.setCurrency("USD");

        when(orderService.createOrder("",any(com.retail.platform.order.entity.Order.class)))
                .thenReturn(order);

        CreateOrderRequest request = new CreateOrderRequest(
                "CUST-1001",
                new BigDecimal("149.99"),
                "USD"
        );

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.customerId").value("CUST-1001"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalAmount").value(149.99));
    }
}