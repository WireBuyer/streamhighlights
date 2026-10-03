package com.wirebuyer.twilight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
@EnableKafkaStreams
public class TwilightApplication {
    public static void main(String[] args) {
        SpringApplication.run(TwilightApplication.class, args);
    }
}
