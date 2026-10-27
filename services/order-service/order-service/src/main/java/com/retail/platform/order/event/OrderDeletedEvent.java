package com.retail.platform.order.event;

import java.util.UUID;

public record OrderDeletedEvent(
        UUID eventId,
        Long orderId
) {
}