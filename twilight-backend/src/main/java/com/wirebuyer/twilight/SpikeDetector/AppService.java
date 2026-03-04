package com.wirebuyer.twilight.SpikeDetector;

import com.wirebuyer.twilight.SpikeDetector.domain.SpikeEvent;
import com.wirebuyer.twilight.SpikeDetector.dto.BroadcastDTO;
import com.wirebuyer.twilight.SpikeDetector.dto.SpikeDTO;
import com.wirebuyer.twilight.SpikeDetector.entity.Broadcast;
import com.wirebuyer.twilight.SpikeDetector.kafka.SpikeSensitivity;
import com.wirebuyer.twilight.SpikeDetector.repo.BroadcastRepository;
import com.wirebuyer.twilight.SpikeDetector.repo.SpikeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static com.wirebuyer.twilight.SpikeDetector.kafka.KafkaTopicConfig.DATABASE_TOPIC;

@Service
public class AppService {

    private final BroadcastRepository broadcastRepository;
    private final SpikeRepository spikeRepository;
    private final KafkaTemplate<String, SpikeEvent> kafkaTemplate;

    public AppService(BroadcastRepository broadcastRepository, SpikeRepository spikeRepository, KafkaTemplate<String, SpikeEvent> kafkaTemplate) {
        this.broadcastRepository = broadcastRepository;
        this.spikeRepository = spikeRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Page<BroadcastDTO> getBroadcasts(String channelName, int page) {
        return broadcastRepository.findByChannelNameIgnoreCase(channelName, PageRequest.of(page, 10))
                .map(BroadcastDTO::toDto);
    }

    public BroadcastDTO getBroadcast(String streamId) {
        return broadcastRepository.findByStreamId(streamId)
                .map(BroadcastDTO::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Broadcast not found"));
    }

    public List<SpikeDTO> getSpikes(Long streamId, SpikeSensitivity sensitivity) {
        return spikeRepository.findByBroadcast_StreamIdAndSensitivity(streamId, sensitivity)
                .stream()
                .map(SpikeDTO::toDto)
                .toList();
    }

    public void submitChannel(String channelName) {
        System.out.println("submitted channel: " + channelName);
    }

    public void submitVod(String vodId) {
        // get logs
        // send each line to the kafka topic
        // when done send the end event with the final log timestamp
        System.out.println("submitted vod: " + vodId);
    }

    public void test() {
        long now = Instant.now().toEpochMilli();

        String streamId = "1234";
        // ensure broadcast exists
        broadcastRepository.findByStreamId(streamId)
                .orElseGet(() -> {
                    Broadcast b = new Broadcast(streamId, "testChannel", streamId, "testTitle", now);
                    b.setEndedAt(now + 300);
                    return broadcastRepository.save(b);
                });

        String sens = "HIGH";
        SpikeEvent spikeEvent = new SpikeEvent(streamId, sens, now, now);
        spikeEvent.spikeEnd = now + 10000;

        kafkaTemplate.send(DATABASE_TOPIC, "test", spikeEvent);
        System.out.println("Sent spike event from test: " + spikeEvent.toString());
    }
}

