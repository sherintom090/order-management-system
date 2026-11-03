
package com.retail.platform.inventory.entity;

import com.retail.platform.inventory.exception.InsufficientStockException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.OffsetDateTime;

@Entity
@Table(name = "inventory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true, length = 100)
    private String productId;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Inventory(String productId, int initialQuantity) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID is required");
        }

        if (initialQuantity < 0) {
            throw new IllegalArgumentException(
                    "Initial quantity cannot be negative");
        }

        this.productId = productId;
        this.availableQuantity = initialQuantity;
        this.reservedQuantity = 0;
    }

    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Reservation quantity must be positive");
        }

        if (availableQuantity < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product: " + productId);
        }

        availableQuantity -= quantity;
        reservedQuantity += quantity;
    }

    public void release(int quantity) {
        if (quantity <= 0 || quantity > reservedQuantity) {
            throw new IllegalArgumentException(
                    "Invalid release quantity");
        }

        reservedQuantity -= quantity;
        availableQuantity += quantity;
    }


    public void confirm(int quantity) {

        if (quantity <= 0 || quantity > reservedQuantity) {
            throw new IllegalArgumentException(
                    "Invalid confirmation quantity");
        }

        // Stock has already been deducted from availableQuantity
        // during reservation. Only reservedQuantity decreases.
        reservedQuantity -= quantity;
    }


    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
