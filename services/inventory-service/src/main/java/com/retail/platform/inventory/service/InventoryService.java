
package com.retail.platform.inventory.service;

import com.retail.platform.inventory.entity.Inventory;
import com.retail.platform.inventory.entity.InventoryReservation;
import com.retail.platform.inventory.entity.ReservationStatus;
import com.retail.platform.inventory.repository.InventoryRepository;
import com.retail.platform.inventory.repository.InventoryReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;

    @Transactional
    public Inventory createInventory(String productId, int quantity) {

        if (inventoryRepository.existsByProductId(productId)) {
            throw new IllegalStateException(
                    "Inventory already exists for product: " + productId);
        }

        Inventory inventory = new Inventory(productId, quantity);

        return inventoryRepository.save(inventory);
    }

    @Transactional(readOnly = true)
    public Inventory getInventory(String productId) {

        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found: " + productId));
    }

    @Transactional
    public void reserveStock(
            String orderId,
            String productId,
            int quantity) {

        // Idempotency check
        if (reservationRepository.existsByOrderIdAndProductId(
                orderId, productId)) {
            return;
        }

        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found: " + productId));

        // Validate and update available stock
        inventory.reserve(quantity);

        // Create reservation record
        InventoryReservation reservation =
                new InventoryReservation(orderId, productId, quantity);

        reservationRepository.save(reservation);

        // Inventory entity is managed by JPA.
        // Hibernate automatically persists its changes on flush.
    }


    @Transactional
    public void confirmReservation(
            String orderId,
            String productId) {

        InventoryReservation reservation =
                reservationRepository
                        .findByOrderIdAndProductId(orderId, productId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Reservation not found"));

        // Idempotent confirmation
        if (reservation.getStatus() ==
                ReservationStatus.CONFIRMED) {
            return;
        }

        if (reservation.getStatus() !=
                ReservationStatus.RESERVED) {
            throw new IllegalStateException(
                    "Reservation cannot be confirmed");
        }

        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Inventory not found"));

        inventory.confirm(reservation.getQuantity());
        reservation.confirm();
    }

    @Transactional
    public void releaseReservation(
            String orderId,
            String productId) {

        InventoryReservation reservation =
                reservationRepository
                        .findByOrderIdAndProductId(orderId, productId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Reservation not found"));

        // Idempotent release
        if (reservation.getStatus() ==
                ReservationStatus.RELEASED) {
            return;
        }

        if (reservation.getStatus() !=
                ReservationStatus.RESERVED) {
            throw new IllegalStateException(
                    "Reservation cannot be released");
        }

        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Inventory not found"));

        inventory.release(reservation.getQuantity());
        reservation.release();
    }
}
