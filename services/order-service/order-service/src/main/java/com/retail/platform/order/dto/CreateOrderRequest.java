package com.retail.platform.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateOrderRequest(

        @NotBlank
        @Size(max = 100)
        String customerId,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal totalAmount,

        @NotBlank
        @Size(min = 3, max = 3)
        String currency
) {
}