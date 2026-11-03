
package com.retail.platform.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateInventoryRequest(

        @NotBlank
        @Size(max = 100)
        String productId,

        @Min(0)
        int quantity

) {}
