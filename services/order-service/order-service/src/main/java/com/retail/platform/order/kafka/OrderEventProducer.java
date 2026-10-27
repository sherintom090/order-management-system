package com.retail.platform.order.kafka;

import com.retail.platform.order.event.OrderCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class OrderEventProducer {

    private static final String ORDER_CREATED_TOPIC = "order-created";

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderEventProducer(
            KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, OrderCreatedEvent>>
    publishOrderCreated(OrderCreatedEvent event) {

        String key = event.orderId().toString();

        return kafkaTemplate.send(
                ORDER_CREATED_TOPIC,
                key,
                event
        );
    }
}