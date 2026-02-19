package com.wirebuyer.twilight.SpikeDetector;

import com.wirebuyer.twilight.SpikeDetector.dto.BroadcastDTO;
import com.wirebuyer.twilight.SpikeDetector.dto.SpikeDTO;
import com.wirebuyer.twilight.SpikeDetector.kafka.SpikeSensitivity;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// TODO: either rename this file or move the broadcast endpoints to a separate controller
@RestController
@RequestMapping("/api")
public class AppController {
    private final AppService appService;

    public AppController(AppService appService) {
        this.appService = appService;
    }

    // get all broadcasts for a channel
    @GetMapping("/broadcasts")
    public Page<BroadcastDTO> getBroadcasts(@RequestParam String channelName,
                                            @RequestParam(defaultValue = "0") int page) {
        return appService.getBroadcasts(channelName, page);
    }

    @GetMapping("/broadcasts/{streamId}")
    public BroadcastDTO getBroadcast(@PathVariable String streamId) {
        return appService.getBroadcast(streamId);
    }

    // get spikes for a broadcast
    @GetMapping("/broadcasts/{streamId}/spikes")
    public List<SpikeDTO> getSpikes(@PathVariable Long streamId,
                                    @RequestParam(defaultValue = "LOW") SpikeSensitivity sensitivity) {
        return appService.getSpikes(streamId, sensitivity);
    }

    // submit a channel to be watched
    @PutMapping("/channels/{channelName}")
    public void submitChannel(@PathVariable String channelName) {
        appService.submitChannel(channelName);
    }

    // submit a vod to be processed
    // maybe make this a list later to process multiple vods of a channel
    @PostMapping("/broadcasts")
    public void submitVod(@RequestBody String vodId) {
        appService.submitVod(vodId);
    }

    @GetMapping("/test")
    public void test() {
        appService.test();
    }
}
