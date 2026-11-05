package com.retail.platform.order.client.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReserveStockRequest(
        @NotBlank @Size(max = 100) String orderId,
        @NotBlank @Size(max = 100) String productId,
        @Min(1) int quantity
) {}