package com.retail.platform.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/kafka")
@RequiredArgsConstructor
public class KafkaBackpressureController {

    private final KafkaListenerEndpointRegistry registry;

    @PostMapping("/pause")
    public ResponseEntity<String> pause() {

        MessageListenerContainer container =
                registry.getListenerContainer("avroOrderListener");

        if (container == null) {
            return ResponseEntity.notFound().build();
        }

        container.pause();

        return ResponseEntity.ok("Kafka consumer pause requested");
    }

    @PostMapping("/resume")
    public ResponseEntity<String> resume() {

        MessageListenerContainer container =
                registry.getListenerContainer("avroOrderListener");

        if (container == null) {
            return ResponseEntity.notFound().build();
        }

        container.resume();

        return ResponseEntity.ok("Kafka consumer resume requested");
    }
}