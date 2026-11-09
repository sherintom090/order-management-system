
package com.retail.platform.inventory.controller;

import com.retail.platform.inventory.dto.CreateInventoryRequest;
import com.retail.platform.inventory.dto.InventoryResponse;
import com.retail.platform.inventory.service.InventoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(
            @Valid @RequestBody CreateInventoryRequest request) {

        var inventory = inventoryService.createInventory(
                request.productId(),
                request.quantity()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(InventoryResponse.from(inventory));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getInventory(
            @PathVariable String productId) {

        log.info("Inventory Service received  request");

        var inventory = inventoryService.getInventory(productId);

        return ResponseEntity.ok(
                InventoryResponse.from(inventory)
        );
    }
}
