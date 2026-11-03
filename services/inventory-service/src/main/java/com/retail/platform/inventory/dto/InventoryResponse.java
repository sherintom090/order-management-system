
package com.retail.platform.inventory.dto;

import com.retail.platform.inventory.entity.Inventory;

public record InventoryResponse(
        String productId,
        int availableQuantity,
        int reservedQuantity
) {

    public static InventoryResponse from(Inventory inventory) {
        return new InventoryResponse(
                inventory.getProductId(),
                inventory.getAvailableQuantity(),
                inventory.getReservedQuantity()
        );
    }
}
