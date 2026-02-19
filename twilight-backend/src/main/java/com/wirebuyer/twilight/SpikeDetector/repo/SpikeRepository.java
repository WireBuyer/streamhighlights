package com.wirebuyer.twilight.SpikeDetector.repo;

import com.wirebuyer.twilight.SpikeDetector.entity.Spike;
import com.wirebuyer.twilight.SpikeDetector.kafka.SpikeSensitivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpikeRepository extends JpaRepository<Spike, Long> {
    List<Spike> findByBroadcast_StreamIdAndSensitivity(Long streamId, SpikeSensitivity sensitivity);

}
