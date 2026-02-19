package com.wirebuyer.twilight.SpikeDetector.twitch;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.StreamList;
import com.wirebuyer.twilight.SpikeDetector.domain.EventMessage;
import com.wirebuyer.twilight.SpikeDetector.domain.EventType;
import com.wirebuyer.twilight.SpikeDetector.entity.Broadcast;
import com.wirebuyer.twilight.SpikeDetector.repo.BroadcastRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wirebuyer.twilight.SpikeDetector.kafka.KafkaTopicConfig.MESSAGES_TOPIC;

// consider moving this file and package
@Component
public class TwitchIngestion {

    private final KafkaTemplate<String, EventMessage> chatKafkaTemplate;
    private final BroadcastRepository broadcastRepository;
    private final TwitchClient twitchClient;

    List<String> watchedChannels = new ArrayList<>(List.of("loltyler1"));
    Map<String, String> livestreams = new HashMap<>();

    // values are injected during class construction so we cannot have fields (returns null), we must have params
    public TwitchIngestion(KafkaTemplate<String, EventMessage> chatKafkaTemplate,
                           BroadcastRepository broadcastRepository,
                           @Value("${TWITCH_OAUTH_CLIENT_ID}") String twitchClientId,
                           @Value("${TWITCH_OAUTH_CLIENT_SECRET}") String twitchClientSecret) {
        this.chatKafkaTemplate = chatKafkaTemplate;
        this.broadcastRepository = broadcastRepository;

        this.twitchClient = TwitchClientBuilder.builder()
                .withClientId(twitchClientId)
                .withClientSecret(twitchClientSecret)
                .withEnableHelix(true)
                .withEnableEventSocket(true)
                .withEnableChat(true)
                .build();
    }

    // use the handlers
    @EventListener(ApplicationReadyEvent.class)
    private void init() {
        // join live streams on startup
        List<Stream> streams = checkActiveStreams(watchedChannels);
        // TODO: factor out saving the broadcast to the db
        streams.forEach(stream -> {
            String broadcasterId = stream.getUserId();
            String channelName = stream.getUserLogin();
            String streamId = stream.getId();
            String title = stream.getTitle();
            long startedAt = stream.getStartedAtInstant().toEpochMilli();
            livestreams.put(stream.getUserLogin(), stream.getId());
            twitchClient.getChat().joinChannel(stream.getUserLogin());

            // check if the broadcast exists and create a record if it doesn't
            Broadcast broadcast = broadcastRepository.findByStreamId(streamId).orElse(null);
            if (broadcast == null) {
                broadcast = new Broadcast(broadcasterId, channelName, streamId, title, startedAt);
                broadcastRepository.save(broadcast);
            }
        });

        // register event listeners for checking if streams go live or offline
        twitchClient.getEventManager().onEvent(ChannelGoLiveEvent.class, event -> {
            Stream stream = event.getStream();
            String broadcasterId = stream.getUserId();
            String channelName = stream.getUserLogin();
            String streamId = stream.getId();
            String title = stream.getTitle();
            long startedAt = stream.getStartedAtInstant().toEpochMilli();

            livestreams.put(channelName, streamId);
            twitchClient.getChat().joinChannel(channelName);

            Broadcast broadcast = new Broadcast(broadcasterId, channelName, streamId, title, startedAt);
            broadcastRepository.save(broadcast);
        });

        twitchClient.getEventManager().onEvent(ChannelGoOfflineEvent.class, event -> {
            String channelName = event.getChannel().getName();
            String streamId = livestreams.get(channelName);
            long timestamp = Instant.now().toEpochMilli();

            String outputMsg = channelName + " - " + streamId + " - ended";
            livestreams.remove(channelName);

            EventMessage eventMessage = new EventMessage(streamId, timestamp, EventType.END_STREAM, outputMsg);
            chatKafkaTemplate.send(MESSAGES_TOPIC, streamId, eventMessage);

            // set the time which the processing finished
            Broadcast broadcast = broadcastRepository.findByStreamId(streamId).orElseThrow();
            broadcast.setEndedAt(timestamp);
            broadcastRepository.save(broadcast);
        });

        twitchClient.getEventManager().onEvent(ChannelMessageEvent.class, event -> {
            String streamId = livestreams.get(event.getChannel().getName());
            // this is for the case where the stream has ended but a message still falls through
            if (streamId == null) return;
            long timestamp = event.getFiredAtInstant().toEpochMilli();
            String message = event.getMessage();

            EventMessage eventMessage = new EventMessage(streamId, timestamp, EventType.CHAT_MESSAGE, message);
            chatKafkaTemplate.send(MESSAGES_TOPIC, streamId, eventMessage);
        });

        watchedChannels.forEach(channel -> {
            twitchClient.getClientHelper().enableStreamEventListener(channel);
        });
    }

    private List<Stream> checkActiveStreams(List<String> channels) {
        StreamList streams = twitchClient.getHelix().getStreams(
                null, null, null, 100, null, null, null, channels
        ).execute();
        return streams.getStreams();
    }
}


