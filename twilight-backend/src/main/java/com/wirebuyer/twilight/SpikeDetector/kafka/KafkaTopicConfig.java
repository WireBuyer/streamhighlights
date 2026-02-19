package com.wirebuyer.twilight.SpikeDetector.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    public static final String MESSAGES_TOPIC = "chat_messages";
    public static final String SPIKE_TOPIC = "spikes";
    public static final String DATABASE_TOPIC = "db";
    public static final String NOTIFICATION_TOPIC = "notification";

    @Bean
    public NewTopic messageTopic() {
        return TopicBuilder.name(MESSAGES_TOPIC)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "60000")
                .build();
    }

    @Bean
    public NewTopic spikeTopic() {
        return TopicBuilder.name(SPIKE_TOPIC)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "300000")
                .build();
    }

    @Bean
    public NewTopic dbTopic() {
        return TopicBuilder.name(DATABASE_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder.name(NOTIFICATION_TOPIC)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "60000")
                .build();
    }

}
