
package com.retail.platform.inventory.controller;

import com.retail.platform.inventory.dto.ReserveStockRequest;
import com.retail.platform.inventory.dto.ReservationActionRequest;
import com.retail.platform.inventory.service.InventoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/inventory/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<Void> reserveStock(
            @Valid @RequestBody ReserveStockRequest request) {

        inventoryService.reserveStock(
                request.orderId(),
                request.productId(),
                request.quantity()
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmReservation(
            @Valid @RequestBody ReservationActionRequest request) {

        inventoryService.confirmReservation(
                request.orderId(),
                request.productId()
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/release")
    public ResponseEntity<Void> releaseReservation(
            @Valid @RequestBody ReservationActionRequest request) {

        inventoryService.releaseReservation(
                request.orderId(),
                request.productId()
        );

        return ResponseEntity.noContent().build();
    }
}
