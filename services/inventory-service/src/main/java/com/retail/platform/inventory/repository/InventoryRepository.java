
package com.retail.platform.inventory.repository;

import com.retail.platform.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository
        extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(String productId);

    boolean existsByProductId(String productId);
}
