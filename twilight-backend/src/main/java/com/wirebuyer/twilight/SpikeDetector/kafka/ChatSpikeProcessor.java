package com.wirebuyer.twilight.SpikeDetector.kafka;

import com.wirebuyer.twilight.SpikeDetector.domain.EventMessage;
import com.wirebuyer.twilight.SpikeDetector.domain.EventType;
import com.wirebuyer.twilight.SpikeDetector.domain.SpikeEvent;
import com.wirebuyer.twilight.SpikeDetector.domain.StreamState;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.ContextualFixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

// TODO: handle the case where i join a stream but the app crashes during the warmup period.
// leads to an uninitialized rate that may trigger a false positive
public class ChatSpikeProcessor extends ContextualFixedKeyProcessor<String, EventMessage, SpikeEvent> {
    private KeyValueStore<String, StreamState> streamStore;
    private KeyValueStore<String, SpikeEvent> spikeStore;

    private final String streamStoreName;
    private final String spikeStoreName;

    // consider refactoring these vars
    private final double SECONDS = 7.0;
    private final double MIN_RATE = 0.4;
    private final double MIN_DURATION = 1000;
    private final int GRACE_PERIOD = 4500;
    double ALPHA;
    private final SpikeSensitivity sensitivity;

    public ChatSpikeProcessor(SpikeSensitivity sensitivity, String streamStoreName, String spikeStoreName) {
        this.sensitivity = sensitivity;
        this.streamStoreName = streamStoreName;
        this.spikeStoreName = spikeStoreName;
    }

    @Override
    public void init(FixedKeyProcessorContext<String, SpikeEvent> context) {
        super.init(context);
        streamStore = context.getStateStore(streamStoreName);
        spikeStore = context.getStateStore(spikeStoreName);

        // run every 15 minutes
        context.schedule(Duration.ofMinutes(15), PunctuationType.WALL_CLOCK_TIME, this::punctuate);
        // run every 2 hours to remove old unmarked streams
        context.schedule(Duration.ofHours(2), PunctuationType.WALL_CLOCK_TIME, this::punctuateOldEntries);
    }

    @Override
    // add logic to decay the ewma if there was no activity for multiple seconds
    public void process(FixedKeyRecord<String, EventMessage> record) {
        String streamId = record.key();
        if (streamId == null) {
            System.out.println("NULL KEY. record: " + record);
        }
        long currentTs = record.value().getTimestamp();

        // check if it's an end event and set the end timestamp
        if (record.value().getEventType() == EventType.END_STREAM) {
            StreamState state = streamStore.get(streamId);
            if (state == null) return;
            state.endTs = currentTs;
            streamStore.put(streamId, state);

            return;
        }

        StreamState state = streamStore.get(streamId);
        if (state == null) {
            state = new StreamState();
            state.joinTime = currentTs;
            String outputMsg = "-----Joined " + streamId + " at: " + new Date(currentTs) + "-----";
            System.out.println(outputMsg);
        }

        state.timestamps.add(currentTs);
        // evict timestamps older than x seconds
        while (!state.timestamps.isEmpty() &&
                currentTs - state.timestamps.peekFirst() > SECONDS * 1000) {
            state.timestamps.removeFirst();
        }
        double currentRate = state.timestamps.size() / SECONDS;

        SpikeEvent spikeEvent = spikeStore.get(streamId);
        // make the ewma adjust slower when there is an active spike
        ALPHA = spikeEvent == null ? 0.03 : 0.01;

        updateEwmaRate(state, currentRate, currentTs);
        streamStore.put(streamId, state);

        double SPIKE_THRESHOLD = sensitivity.calculateThreshold(state.ewmaRate);

        // determine if it's a spike based on a ratio of the current rate to the ewma rate
        // only works if the stream has been running for at least 30 seconds
        boolean isSpiking = currentRate / state.ewmaRate > SPIKE_THRESHOLD &&
                state.ewmaRate > MIN_RATE &&
                currentTs - state.joinTime > Duration.ofSeconds(30).toMillis();

        spikeEvent = processSpike(spikeEvent, record, state, isSpiking);
        if (spikeEvent == null) {
            spikeStore.delete(streamId);
        } else {
            spikeStore.put(streamId, spikeEvent);
        }

//        String formatted = String.format("C: %.2f | R: %.2f | N: %.2f | T: %.3f",
//                currentRate, state.ewmaRate, SPIKE_THRESHOLD * state.ewmaRate, SPIKE_THRESHOLD);
//
//        System.out.print("\r" + formatted);
    }

    private void updateEwmaRate(StreamState state, double currentRate, long currentTs) {
        // add a cooldown period using streamstate.lastSpike later
        // if (currentTs - state.lastSpike > 5000) {
        //    state.ewmaRate = state.ewmaRate * (1 - ALPHA) + currentRate * ALPHA;
        //    state.lastEwmaUpdate = currentTs;
        //  }

        // update the ewma up to once per second
        if (currentTs - state.lastEwmaUpdate > 1000) {
            state.ewmaRate = state.ewmaRate * (1 - ALPHA) + currentRate * ALPHA;
            state.lastEwmaUpdate = currentTs;
        }

    }

    private SpikeEvent processSpike(SpikeEvent spikeEvent,
                                    FixedKeyRecord<String, EventMessage> record,
                                    StreamState state,
                                    boolean isSpiking
    ) {
        String streamId = record.key();
        long currentTs = record.value().getTimestamp();
        long COOLDOWN_PERIOD = Duration.ofSeconds(10).toMillis();

        if (spikeEvent == null) {
            // create a new spike event when it starts spiking
            if (isSpiking && currentTs - state.lastSpike > COOLDOWN_PERIOD) {
//                System.out.println("-----Started spike | creating event at: " + new Date(currentTs) + "-----");
                return new SpikeEvent(streamId, sensitivity.name(), currentTs, currentTs);
            } else {
                return null;
            }
        }

        // update the last time the spike was updated and return it
        if (isSpiking) {
            spikeEvent.lastUpdate = currentTs;

            // send a notification if it lasted long enough and the notification hasn't been sent yet
            if (currentTs - spikeEvent.spikeStart > MIN_DURATION &&
                    !spikeEvent.sentNotification) {
                context().forward(record.withValue(spikeEvent));
                spikeEvent.sentNotification = true;
            }
            return spikeEvent;
        }

        // handle the case when the spikeEvent exists, but it's not spiking anymore
        // if it doesn't last long enough, delete it
        if (currentTs - spikeEvent.spikeStart < MIN_DURATION) {
//            System.out.println("-----spike did not last long enough - deleting spike at: " + new Date(currentTs) + "-----\n");
            return null;
        }
        // if it did last long enough, save it. includes a grace period
        if (currentTs - spikeEvent.lastUpdate > GRACE_PERIOD) {
            // Date date = new Date(currentTs);
//            System.out.println("-----Ended spike | Lasted: " + (currentTs - spikeEvent.spikeStart) + " | " + date + "-----\n");
            spikeEvent.spikeEnd = currentTs;
            state.lastSpike = currentTs;

            // send it out with the db key
            context().forward(record.withValue(spikeEvent));
            return null;
        }
        return spikeEvent;
    }

    // deletes finished stream states if at least 5 minutes have passed since the stream ended
    private void punctuate(long timestamp) {
        long minTime = TimeUnit.MINUTES.toMillis(5);

        // iterate over the state store keys
        try (KeyValueIterator<String, StreamState> iterator = streamStore.all()) {
            while (iterator.hasNext()) {
                KeyValue<String, StreamState> next = iterator.next();
                if (next.value.endTs != null &&
                        next.value.endTs + minTime < Instant.now().toEpochMilli()) {
                    streamStore.delete(next.key);
                    spikeStore.delete(next.key);
                }
            }
        }
    }

    // backup punctuator to delete streams that were recorded but were never marked to deleted.
    // happens if the program is stopped when the event comes through
    private void punctuateOldEntries(long timestamp) {
        long cutoffTime = TimeUnit.HOURS.toMillis(20);

        try (KeyValueIterator<String, StreamState> iterator = streamStore.all()) {
            while (iterator.hasNext()) {
                KeyValue<String, StreamState> next = iterator.next();
                StreamState state = next.value;
                if (state.lastSpike + cutoffTime < Instant.now().toEpochMilli()) {
                    streamStore.delete(next.key);
                    spikeStore.delete(next.key);
                }
            }
        }
    }
}
