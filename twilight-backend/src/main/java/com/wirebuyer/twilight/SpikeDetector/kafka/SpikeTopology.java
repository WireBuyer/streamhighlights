package com.wirebuyer.twilight.SpikeDetector.kafka;

import com.wirebuyer.twilight.SpikeDetector.domain.EventMessage;
import com.wirebuyer.twilight.SpikeDetector.domain.SpikeEvent;
import com.wirebuyer.twilight.SpikeDetector.domain.StreamState;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.TopicNameExtractor;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.wirebuyer.twilight.SpikeDetector.kafka.KafkaTopicConfig.DATABASE_TOPIC;
import static com.wirebuyer.twilight.SpikeDetector.kafka.KafkaTopicConfig.NOTIFICATION_TOPIC;

@Configuration
public class SpikeTopology {
    public static final String STREAM_STATE_STORE = "stream_store";
    public static final String SPIKE_EVENT_STORE = "spike_store";

    @Bean
    public KStream<String, EventMessage> kStream(StreamsBuilder builder) {
        KStream<String, EventMessage> messageStream = builder.stream(KafkaTopicConfig.MESSAGES_TOPIC,
                Consumed.with(Serdes.String(), JsonSerdes.chatMessageSerde()));

        for (SpikeSensitivity sensitivity : SpikeSensitivity.values()) {
            String streamStoreName = STREAM_STATE_STORE + "_" + sensitivity.name().toLowerCase();
            String spikeStoreName = SPIKE_EVENT_STORE + "_" + sensitivity.name().toLowerCase();

            StoreBuilder<KeyValueStore<String, StreamState>> streamStoreBuilder =
                    Stores.keyValueStoreBuilder(
                            Stores.persistentKeyValueStore(streamStoreName),
                            Serdes.String(),
                            JsonSerdes.streamStateSerde()
                    );

            StoreBuilder<KeyValueStore<String, SpikeEvent>> spikeStoreBuilder =
                    Stores.keyValueStoreBuilder(
                            Stores.persistentKeyValueStore(spikeStoreName),
                            Serdes.String(),
                            JsonSerdes.spikeEventSerde()
                    );

            builder.addStateStore(streamStoreBuilder);
            builder.addStateStore(spikeStoreBuilder);

            // pass the state store names into the constructor. also need to pass the names to process() itself
            messageStream.processValues(() -> new ChatSpikeProcessor(sensitivity, streamStoreName, spikeStoreName), streamStoreName, spikeStoreName)
                    .to(topicNameExtractor, Produced.with(Serdes.String(), JsonSerdes.spikeEventSerde()));
        }
        return messageStream;
    }

    private final TopicNameExtractor<String, SpikeEvent> topicNameExtractor = (key, value, recordContext) -> {
        if (!value.sentNotification) {
            return NOTIFICATION_TOPIC;
        } else {
            return DATABASE_TOPIC;
        }
    };
}