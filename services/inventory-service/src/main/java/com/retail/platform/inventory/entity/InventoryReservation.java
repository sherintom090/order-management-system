
package com.retail.platform.inventory.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "inventory_reservation",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_order_product",
                columnNames = {"order_id", "product_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, length = 100)
    private String orderId;

    @Column(name = "product_id", nullable = false, length = 100)
    private String productId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public InventoryReservation(
            String orderId,
            String productId,
            int quantity) {

        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID is required");
        }

        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID is required");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Reservation quantity must be positive");
        }

        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = ReservationStatus.RESERVED;
    }

    public void confirm() {
        if (status != ReservationStatus.RESERVED) {
            throw new IllegalStateException(
                    "Only reserved inventory can be confirmed");
        }

        status = ReservationStatus.CONFIRMED;
    }

    public void release() {
        if (status != ReservationStatus.RESERVED) {
            throw new IllegalStateException(
                    "Only reserved inventory can be released");
        }

        status = ReservationStatus.RELEASED;
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
