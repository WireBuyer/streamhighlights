package com.wirebuyer.twilight.SpikeDetector.dto;

import com.wirebuyer.twilight.SpikeDetector.entity.Spike;
import com.wirebuyer.twilight.SpikeDetector.kafka.SpikeSensitivity;

public record SpikeDTO(
        SpikeSensitivity sensitivity,
        Long spikeStart,
        Long spikeEnd) {
    public static SpikeDTO toDto(Spike spike) {
        return new SpikeDTO(
                spike.getSensitivity(),
                spike.getSpikeStart(),
                spike.getSpikeEnd());
    }
}
