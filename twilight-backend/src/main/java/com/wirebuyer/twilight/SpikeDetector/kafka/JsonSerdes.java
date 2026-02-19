package com.wirebuyer.twilight.SpikeDetector.kafka;

import com.wirebuyer.twilight.SpikeDetector.domain.EventMessage;
import com.wirebuyer.twilight.SpikeDetector.domain.SpikeEvent;
import com.wirebuyer.twilight.SpikeDetector.domain.StreamState;
import org.springframework.kafka.support.serializer.JacksonJsonSerde;

public class JsonSerdes {
    public static JacksonJsonSerde<EventMessage> chatMessageSerde() {
        return new JacksonJsonSerde<EventMessage>(EventMessage.class);
    }

    public static JacksonJsonSerde<StreamState> streamStateSerde() {
        return new JacksonJsonSerde<StreamState>(StreamState.class);
    }

    public static JacksonJsonSerde<SpikeEvent> spikeEventSerde() {
        return new JacksonJsonSerde<SpikeEvent>(SpikeEvent.class);
    }

}
