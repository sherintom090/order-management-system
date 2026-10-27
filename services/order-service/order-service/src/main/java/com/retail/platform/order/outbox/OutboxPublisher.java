package com.retail.platform.order.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.platform.order.event.OrderCreatedEvent;
import com.retail.platform.order.kafka.OrderEventProducer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final OrderEventProducer orderEventProducer;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            OrderEventProducer orderEventProducer,
            ObjectMapper objectMapper) {

        this.outboxEventRepository = outboxEventRepository;
        this.orderEventProducer = orderEventProducer;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                outboxEventRepository
                        .findTop100ByStatusOrderByCreatedAtAsc("PENDING");

        for (OutboxEvent outboxEvent : events) {
            publish(outboxEvent);
        }
    }

    private void publish(OutboxEvent outboxEvent) {

        try {
            OrderCreatedEvent event =
                    objectMapper.readValue(
                            outboxEvent.getPayload(),
                            OrderCreatedEvent.class
                    );

            orderEventProducer.publishOrderCreated(event)
                    .whenComplete((result, ex) -> {

                        if (ex == null) {

                            outboxEvent.setStatus("PUBLISHED");
                            outboxEvent.setPublishedAt(OffsetDateTime.now());

                            outboxEventRepository.save(outboxEvent);

                            var metadata = result.getRecordMetadata();

                            System.out.printf(
                                    "Outbox publish SUCCESS: outboxId=%d, topic=%s, partition=%d, offset=%d%n",
                                    outboxEvent.getId(),
                                    metadata.topic(),
                                    metadata.partition(),
                                    metadata.offset()
                            );

                        } else {

                            System.err.printf(
                                    "Outbox publish FAILED: outboxId=%d, error=%s%n",
                                    outboxEvent.getId(),
                                    ex.getMessage()
                            );
                        }
                    });

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to deserialize outbox event: "
                            + outboxEvent.getId(),
                    e
            );
        }
    }
}