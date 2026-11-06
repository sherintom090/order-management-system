package com.retail.platform.inventory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationCoordinator {

    private final InventoryService inventoryService;

    public void reserveStock(
            String orderId,
            String productId,
            int quantity) {

        inventoryService.reserveStock(
                orderId,
                productId,
                quantity
        );
    }
}