package com.retail.platform.inventory;

import com.retail.platform.inventory.entity.Inventory;
import com.retail.platform.inventory.repository.InventoryRepository;
import com.retail.platform.inventory.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InventoryConcurrencyIntegrationTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    void shouldPreventOversellingWithConcurrentReservations()
            throws Exception {

        String productId = "CONCURRENT-" + UUID.randomUUID();

        // Create inventory with 5 available units
        inventoryRepository.saveAndFlush(
                new Inventory(productId, 5)
        );

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Callable<Boolean> reservationTask = () -> {

                ready.countDown();

                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Start timed out");
                }

                String orderId = UUID.randomUUID().toString();

                try {
                    inventoryService.reserveStock(
                            orderId,
                            productId,
                            4
                    );
                    return true;

                } catch (RuntimeException ex) {
                    return false;
                }
            };

            Future<Boolean> first = executor.submit(reservationTask);
            Future<Boolean> second = executor.submit(reservationTask);

            assertTrue(ready.await(10, TimeUnit.SECONDS));

            // Release both threads together
            start.countDown();

            boolean firstSuccess = first.get(15, TimeUnit.SECONDS);
            boolean secondSuccess = second.get(15, TimeUnit.SECONDS);

            long successfulReservations =
                    (firstSuccess ? 1 : 0)
                            + (secondSuccess ? 1 : 0);

            assertEquals(1, successfulReservations);

            // Read fresh state after both transactions finish
            Inventory inventory = inventoryRepository
                    .findByProductId(productId)
                    .orElseThrow();

            assertEquals(1, inventory.getAvailableQuantity());
            assertEquals(4, inventory.getReservedQuantity());

        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }
}