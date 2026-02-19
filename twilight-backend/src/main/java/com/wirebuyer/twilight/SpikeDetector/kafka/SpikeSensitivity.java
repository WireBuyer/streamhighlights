package com.wirebuyer.twilight.SpikeDetector.kafka;

public enum SpikeSensitivity {
    HIGH(1.4, 3.5, 0.4),
    MEDIUM(1.6, 3.5, 0.35),
    LOW(1.9, 3.5, 0.3);

    public final double base;
    public final double multiplier;
    public final double decay;

    SpikeSensitivity(double base, double multiplier, double decay) {
        this.base = base;
        this.multiplier = multiplier;
        this.decay = decay;
    }

    public double calculateThreshold(double ewmaRate) {
        return base + multiplier * Math.exp(-decay * ewmaRate);
    }
}
