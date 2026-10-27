package com.retail.platform.order.kafka;

import com.retail.platform.order.event.OrderCreatedEvent;
import com.retail.platform.order.outbox.ProcessedEvent;
import com.retail.platform.order.outbox.ProcessedEventRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;

@Component
public class OrderEventConsumer {

    private final ProcessedEventRepository processedEventRepository;

    public OrderEventConsumer(
            ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }

    @KafkaListener(
            topics = "order-created",
            groupId = "order-service-learning"
    )
    @Transactional
    public void consume(OrderCreatedEvent event) {

        if (processedEventRepository.existsById(event.eventId())) {

            System.out.println(
                    "Duplicate event skipped: " + event.eventId()
            );

            return;
        }

        // Later this will be real inventory/payment business logic.
        System.out.println(
                "Processing OrderCreatedEvent: " + event
        );

        ProcessedEvent processedEvent = new ProcessedEvent();

        processedEvent.setEventId(event.eventId());
        processedEvent.setEventType("ORDER_CREATED");
        processedEvent.setProcessedAt(OffsetDateTime.now());

        processedEventRepository.save(processedEvent);
    }
}