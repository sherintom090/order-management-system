
package com.retail.platform.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReservationActionRequest(

        @NotBlank
        @Size(max = 100)
        String orderId,

        @NotBlank
        @Size(max = 100)
        String productId

) {}
