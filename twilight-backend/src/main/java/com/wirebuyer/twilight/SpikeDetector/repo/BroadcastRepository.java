package com.wirebuyer.twilight.SpikeDetector.repo;

import com.wirebuyer.twilight.SpikeDetector.entity.Broadcast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BroadcastRepository extends JpaRepository<Broadcast, Long> {
    Optional<Broadcast> findByStreamId(String id);

    Page<Broadcast> findByChannelName(String channelName, Pageable pageable);
}
