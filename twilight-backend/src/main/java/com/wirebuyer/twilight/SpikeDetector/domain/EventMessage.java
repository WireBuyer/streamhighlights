package com.wirebuyer.twilight.SpikeDetector.domain;

@SuppressWarnings("unused")
// consider making this the base class for events
public class EventMessage {
    private String streamId;
    private long timestamp;
    private EventType eventType;
    private String payload;

    public EventType getEventType() {
        return eventType;
    }

    public EventMessage() {
    }

    public EventMessage(String streamId, long timestamp, EventType eventType, String payload) {
        this.streamId = streamId;
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.payload = payload;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPayload() {
        return payload;
    }

    public void getPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "EventMessage{" +
                "streamId='" + streamId + '\'' +
                ", timestamp=" + timestamp +
                ", payload='" + payload + '\'' +
                '}';
    }
}
