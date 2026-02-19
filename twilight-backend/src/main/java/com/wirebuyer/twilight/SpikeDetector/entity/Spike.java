package com.wirebuyer.twilight.SpikeDetector.entity;

import com.wirebuyer.twilight.SpikeDetector.domain.SpikeEvent;
import com.wirebuyer.twilight.SpikeDetector.kafka.SpikeSensitivity;
import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "spikes", indexes = {
        @Index(name = "idx_spike_broadcast_id", columnList = "broadcast_id"),
        @Index(name = "idx_spike_sensitivity", columnList = "sensitivity")
})
public class Spike {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "broadcast_id")
    @NotNull
    private Broadcast broadcast;

    @NotNull
    @Enumerated(EnumType.STRING)
    private SpikeSensitivity sensitivity;

    private Long spikeStart;

    private Long spikeEnd;

    protected Spike() {
    }

    public Spike(SpikeEvent spikeEvent) {
        // TODO: make this an enum from the get go
        this.sensitivity = SpikeSensitivity.valueOf(spikeEvent.sensitivity);
        this.spikeStart = spikeEvent.spikeStart;
        this.spikeEnd = spikeEvent.spikeEnd;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Broadcast getBroadcast() {
        return broadcast;
    }

    public void setBroadcast(Broadcast broadcast) {
        this.broadcast = broadcast;
    }

    public @NotNull SpikeSensitivity getSensitivity() {
        return sensitivity;
    }

    public Long getSpikeStart() {
        return spikeStart;
    }

    public Long getSpikeEnd() {
        return spikeEnd;
    }
}
