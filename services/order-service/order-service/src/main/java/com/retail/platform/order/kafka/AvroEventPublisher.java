package com.retail.platform.order.kafka;

import com.retail.platform.order.avro.OrderCreatedEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AvroEventPublisher {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public AvroEventPublisher(
            @Qualifier("avroKafkaTemplate")
            KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<Void> publish(OrderCreatedEvent event) {

        return kafkaTemplate
                .send("order-created-avro",
                        String.valueOf(event.getOrderId()),
                        event)
                .thenAccept(result ->
                        System.out.printf(
                                "Avro event published: partition=%d, offset=%d%n",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        ));
    }
}