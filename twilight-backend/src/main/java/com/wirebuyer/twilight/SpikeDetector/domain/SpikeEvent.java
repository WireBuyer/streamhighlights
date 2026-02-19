package com.wirebuyer.twilight.SpikeDetector.domain;

import java.time.Instant;
import java.time.ZoneId;

public class SpikeEvent {
    public String streamId;
    public String sensitivity;
    public Long spikeStart;
    public Long spikeEnd;
    public boolean sentNotification;
    public Long lastUpdate;

    public SpikeEvent() {
    }

    public SpikeEvent(String streamId, String sensitivity, long spikeStart, long lastUpdate) {
        this.streamId = streamId;
        this.sensitivity = sensitivity;
        this.spikeStart = spikeStart;
        this.sentNotification = false;
        this.lastUpdate = lastUpdate;
    }

    @Override
    public String toString() {

        return "SpikeEvent{" +
                "streamId='" + streamId + '\'' +
                ", sensitivity='" + sensitivity + '\'' +
                ", spikeStart=" + Instant.ofEpochMilli(spikeStart).atZone(ZoneId.systemDefault()).toLocalDateTime() +
                ", spikeEnd=" + spikeEnd +
                ", sentNotification=" + sentNotification +
                '}';
    }
}
