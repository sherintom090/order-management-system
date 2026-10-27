package com.retail.platform.order.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.ExponentialBackOff;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<Object, Object> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) ->
                                new TopicPartition(
                                        record.topic() + ".DLT",
                                        record.partition()
                                )
                );

        ExponentialBackOff backOffE = new ExponentialBackOff();

        backOffE.setInitialInterval(1000);
        backOffE.setMultiplier(2.0);
        backOffE.setMaxInterval(10000);

        FixedBackOff backOff =
                new FixedBackOff(
                        2000L,   // 2 seconds between attempts
                        2L       // 2 retries after the first attempt
                );

        return new DefaultErrorHandler(
                recoverer,
                backOff
        );
    }
}