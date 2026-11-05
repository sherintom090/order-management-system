package com.retail.platform.order.client;

import com.retail.platform.order.client.dto.ReserveStockRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

@Component
public class InventoryClient {

    private static final String RESERVATION_PATH =
            "/api/v1/inventory/reservations";

    private final RestClient restClient;

    public InventoryClient(
            @Qualifier("inventoryRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public void reserveStock(
            String orderId,
            String productId,
            int quantity) {

        ReserveStockRequest request =
                new ReserveStockRequest(orderId, productId, quantity);

        try {
            restClient.post()
                    .uri(RESERVATION_PATH)
                    .body(request)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            (req, response) -> {
                                throw new InventoryServiceException(
                                        response.getStatusCode().value(),
                                        "Inventory reservation failed"
                                );
                            }
                    )
                    .toBodilessEntity();

        } catch (InventoryServiceException ex) {
            throw ex;

        } catch (ResourceAccessException ex) {
            throw new InventoryServiceException(
                    503,
                    "Inventory Service is unreachable",
                    ex
            );

        } catch (RestClientResponseException ex) {
            throw new InventoryServiceException(
                    ex.getStatusCode().value(),
                    "Inventory reservation failed",
                    ex
            );
        }
    }
}