
package com.retail.platform.inventory.service;

import com.retail.platform.inventory.entity.Inventory;
import com.retail.platform.inventory.entity.InventoryReservation;
import com.retail.platform.inventory.entity.ReservationStatus;
import com.retail.platform.inventory.repository.InventoryRepository;
import com.retail.platform.inventory.repository.InventoryReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.retail.platform.inventory.exception.ResourceNotFoundException;

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

        if (orderId == null || orderId.isBlank()
                || productId == null || productId.isBlank()
                || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Invalid reservation request");
        }

        var existing = reservationRepository
                .findByOrderIdAndProductId(orderId, productId);

        if (existing.isPresent()) {
            InventoryReservation reservation = existing.get();

            if (reservation.getQuantity() != quantity) {
                throw new IllegalStateException(
                        "Reservation already exists with different quantity");
            }

            if (reservation.getStatus() == ReservationStatus.RESERVED
                    || reservation.getStatus() == ReservationStatus.CONFIRMED) {
                return;
            }

            throw new IllegalStateException(
                    "Reservation has already been released");
        }

        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found: " + productId));

        inventory.reserve(quantity);

        InventoryReservation reservation =
                new InventoryReservation(orderId, productId, quantity);

        reservationRepository.save(reservation);
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
                        new ResourceNotFoundException(
                                "Reservation not found for order: " + orderId));

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
