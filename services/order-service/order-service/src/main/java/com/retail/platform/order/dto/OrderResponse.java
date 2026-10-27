package com.retail.platform.order.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder
public record OrderResponse(
        Long id,
        String customerId,
        String status,
        BigDecimal totalAmount,
        String currency,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}