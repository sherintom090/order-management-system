package com.retail.platform.order.client;

public class InventoryServiceException extends RuntimeException {

    private final int statusCode;

    public InventoryServiceException(
            int statusCode,
            String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public InventoryServiceException(
            int statusCode,
            String message,
            Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}