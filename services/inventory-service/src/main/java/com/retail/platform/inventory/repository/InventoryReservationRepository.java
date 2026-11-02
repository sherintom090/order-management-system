
package com.retail.platform.inventory.repository;

import com.retail.platform.inventory.entity.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryReservationRepository
        extends JpaRepository<InventoryReservation, Long> {

    Optional<InventoryReservation> findByOrderIdAndProductId(
            String orderId,
            String productId
    );

    boolean existsByOrderIdAndProductId(
            String orderId,
            String productId
    );
}
