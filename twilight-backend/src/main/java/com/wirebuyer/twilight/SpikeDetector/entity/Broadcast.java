package com.wirebuyer.twilight.SpikeDetector.entity;

import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Entity
@Table(name = "broadcasts", indexes = {
        @Index(name = "idx_broadcast_stream_id", columnList = "streamId", unique = true),
        @Index(name = "idx_broadcast_vod_id", columnList = "vodId", unique = true),
        @Index(name = "idx_broadcast_channel_name", columnList = "channelName")
})
public class Broadcast {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    private String broadcasterId;

    @NotNull
    private String channelName;

    @NotNull
    private String streamId;

    @NotNull
    private String streamTitle;

    // use this for vod processing
    private String vodId;

    // this is the timestamp for when the stream started
    @NotNull
    private Long startedAt;

    // this is the timestamp for when i finished processing, not when the stream ended. change name
    private Long endedAt;

    @OneToMany(mappedBy = "broadcast", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Spike> spikes;

    public Broadcast() {
    }

    public Broadcast(String broadcasterId, String channelName, String streamId, String streamTitle, Long startedAt) {
        this.broadcasterId = broadcasterId;
        this.channelName = channelName;
        this.streamId = streamId;
        this.streamTitle = streamTitle;
        this.startedAt = startedAt;
    }

    public Long getId() {
        return id;
    }

    public @NotNull String getBroadcasterId() {
        return broadcasterId;
    }

    public @NotNull String getChannelName() {
        return channelName;
    }

    public @NotNull String getStreamId() {
        return streamId;
    }

    public @NotNull String getStreamTitle() {
        return streamTitle;
    }

    public String getVodId() {
        return vodId;
    }

    public void setVodId(String vodId) {
        this.vodId = vodId;
    }

    public @NotNull Long getStartedAt() {
        return startedAt;
    }

    public Long getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Long endedAt) {
        this.endedAt = endedAt;
    }
}

