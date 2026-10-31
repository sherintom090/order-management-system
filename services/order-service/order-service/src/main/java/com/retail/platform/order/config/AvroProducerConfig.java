
package com.retail.platform.order.config;

import com.retail.platform.order.avro.OrderCreatedEvent;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AvroProducerConfig {

    @Bean("avroProducerFactory")
    public ProducerFactory<String, OrderCreatedEvent> avroProducerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${app.kafka.schema-registry-url}") String schemaRegistryUrl) {

        Map<String, Object> props = new HashMap<>();

        // Kafka broker
        props.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        // Serializers
        props.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        props.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                KafkaAvroSerializer.class
        );

        // Confluent Schema Registry
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put("auto.register.schemas", true);

        // Producer reliability
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        // Batching
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 32 * 1024); // 32 KB
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);          // 5 ms
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
// Prevent unlimited blocking during send
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 10000);
// Bound outstanding requests while preserving ordering guarantees
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean("avroKafkaTemplate")
    public KafkaTemplate<String, OrderCreatedEvent> avroKafkaTemplate(
            @Qualifier("avroProducerFactory")
            ProducerFactory<String, OrderCreatedEvent> producerFactory) {

        return new KafkaTemplate<>(producerFactory);
    }
}
