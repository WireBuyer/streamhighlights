package com.wirebuyer.twilight.SpikeDetector.kafka;

import com.wirebuyer.twilight.SpikeDetector.domain.SpikeEvent;
import com.wirebuyer.twilight.SpikeDetector.entity.Broadcast;
import com.wirebuyer.twilight.SpikeDetector.entity.Spike;
import com.wirebuyer.twilight.SpikeDetector.repo.BroadcastRepository;
import com.wirebuyer.twilight.SpikeDetector.repo.SpikeRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static com.wirebuyer.twilight.SpikeDetector.kafka.KafkaTopicConfig.DATABASE_TOPIC;

@Component
public class SpikeEventConsumer {
    private final SpikeRepository spikeRepository;
    private final BroadcastRepository broadcastRepository;

    public SpikeEventConsumer(SpikeRepository spikeRepository, BroadcastRepository broadcastRepository) {
        this.spikeRepository = spikeRepository;
        this.broadcastRepository = broadcastRepository;
    }

    @KafkaListener(topics = DATABASE_TOPIC, groupId = "spike_saver")
    // use payload to skip using contextrecord
    public void dbWriter(@Payload SpikeEvent spikeEvent) {
        // look up the broadcast, use a hashmap later to avoid this
        Broadcast broadcast = broadcastRepository.findByStreamId(spikeEvent.streamId).orElseThrow();

        Spike spike = new Spike(spikeEvent);
        spike.setBroadcast(broadcast);
        spikeRepository.save(spike);
    }
}
