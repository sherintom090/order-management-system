package com.retail.platform.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "idempotency_keys",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_idempotency_key", columnNames = "idempotency_key")
        }
)
@Getter
@Setter
public class IdempotencyKeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}