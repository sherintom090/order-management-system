package com.retail.platform.order.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderCancelledEvent(
        UUID eventId,
        Long orderId,
        String customerId,
        String status,
        OffsetDateTime updatedAt
) {
}