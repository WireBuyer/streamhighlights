package com.wirebuyer.twilight.SpikeDetector.kafka;

import com.wirebuyer.twilight.SpikeDetector.domain.SpikeEvent;
import org.springframework.kafka.annotation.KafkaListener;

import static com.wirebuyer.twilight.SpikeDetector.kafka.KafkaTopicConfig.NOTIFICATION_TOPIC;

public class NotificationConsumer {

    @KafkaListener(topics = NOTIFICATION_TOPIC)
    public void notificationReader(SpikeEvent spikeEvent) {
        // TODO: Send notification
    }
}
