
package com.retail.platform.order.kafka;

import com.retail.platform.order.avro.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AvroOrderEventConsumer {

    @KafkaListener(
            id = "avroOrderListener",
            topics = "order-created-avro",
            groupId = "order-service-avro-learning",
            containerFactory = "avroKafkaListenerContainerFactory"
    )
    public void consume(
            ConsumerRecord<String, OrderCreatedEvent> record,
            Acknowledgment acknowledgment) {

        OrderCreatedEvent event = record.value();

        try {
            // Simulated business processing
            log.info(
                    "Processing orderId={}, partition={}, offset={}",
                    event.getOrderId(),
                    record.partition(),
                    record.offset()
            );

            // Business processing completed successfully

            log.info("Acknowledged offset={}", record.offset());

        } catch (Exception ex) {
            log.error("Failed to process orderId={}", event.getOrderId(), ex);
            throw ex;
        }
    }
}
