package com.retail.platform.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OrderServiceApplication {

    public static void main(String[] args) {
        // DevTools' restart classloader breaks Avro SpecificRecord deserialization
        // (KafkaAvroDeserializer loads the class from a different classloader)
        System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(OrderServiceApplication.class, args);
    }

}
