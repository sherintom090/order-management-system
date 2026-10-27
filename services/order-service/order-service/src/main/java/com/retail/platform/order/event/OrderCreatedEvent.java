package com.retail.platform.order.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        Long orderId,
        String customerId,
        BigDecimal totalAmount,
        String currency,
        String status,
        OffsetDateTime createdAt
) {
}