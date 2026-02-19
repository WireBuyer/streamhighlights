package com.wirebuyer.twilight.SpikeDetector.domain;

import java.util.ArrayDeque;

public class StreamState {
    public long joinTime = 0;
    // deque for the current sliding window rate
    public ArrayDeque<Long> timestamps = new ArrayDeque<>();

    // start at 3 and let channels adjust
    public double ewmaRate = 3.0;
    public long lastEwmaUpdate = 0;
    public long lastSpike = 0;
//    public long lastMessage = 0;

    public Long endTs = null;

    @Override
    public String toString() {
        return "StreamState{" +
                "joinTime=" + joinTime +
                ", timestamps=" + timestamps.size() +
                ", ewmaRate=" + ewmaRate +
                ", lastEwmaUpdate=" + lastEwmaUpdate +
//                ", lastSpike=" + lastSpike +
//                ", lastMessage=" + lastMessage +
                ", endTs=" + endTs +
                '}';
    }
}


