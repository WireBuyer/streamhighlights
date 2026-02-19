package com.wirebuyer.twilight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@SpringBootApplication
@EnableKafkaStreams
public class TwilightApplication {
    public static void main(String[] args) {
        SpringApplication.run(TwilightApplication.class, args);
    }
}
