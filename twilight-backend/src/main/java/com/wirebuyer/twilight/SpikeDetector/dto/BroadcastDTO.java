package com.wirebuyer.twilight.SpikeDetector.dto;

import com.wirebuyer.twilight.SpikeDetector.entity.Broadcast;

public record BroadcastDTO(
        String channelName,
        String broadcasterId,
        String streamId,
        String streamTitle,
        String vodId,
        Long startedAt) {
    public static BroadcastDTO toDto(Broadcast broadcast) {
        return new BroadcastDTO(
                broadcast.getChannelName(),
                broadcast.getBroadcasterId(),
                broadcast.getStreamId(),
                broadcast.getStreamTitle(),
                broadcast.getVodId(),
                broadcast.getStartedAt()
        );
    }
}
