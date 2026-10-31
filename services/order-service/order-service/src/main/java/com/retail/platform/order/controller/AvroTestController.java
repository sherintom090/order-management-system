package com.retail.platform.order.controller;

import com.retail.platform.order.avro.OrderCreatedEvent;
import com.retail.platform.order.kafka.AvroEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/test/avro")
public class AvroTestController {

    private final AvroEventPublisher publisher;

    public AvroTestController(AvroEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<String>> publishTestEvent(
            @RequestParam long orderId) {

        OrderCreatedEvent event = OrderCreatedEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setOrderId(orderId)
                .setCustomerId("CUSTOMER-101")
                .setTotalAmount(new BigDecimal("249.99").toPlainString())
                .setCurrency("USD")
                .setStatus("CREATED")
                .setCreatedAt(OffsetDateTime.now().toString())
                .setShippingMethod("EXPRESS")
                .build();

        return publisher.publish(event)
                .thenApply(ignored ->
                        ResponseEntity.ok("Avro event published successfully"));
    }
}