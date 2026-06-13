package net.flectone.pulse.module.integration.twitch.listener;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.util.ChatReply;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.integration.twitch.TwitchModule;
import net.flectone.pulse.module.integration.twitch.execution.dispatcher.TwitchCommandDispatcher;
import net.flectone.pulse.module.integration.twitch.sender.TwitchSender;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TwitchMessageListener implements TwitchEventListener<ChannelMessageEvent> {

    private final TwitchModule twitchModule;
    private final TwitchSender twitchSender;
    private final TwitchCommandDispatcher twitchCommandDispatcher;

    public Class<ChannelMessageEvent> getEventType() {
        return ChannelMessageEvent.class;
    }

    @Override
    public void execute(ChannelMessageEvent event) {
        if (twitchCommandDispatcher.executeCommand(event)) return;

        List<String> channel = twitchModule.config().messageChannel().get(twitchModule.name().name());
        if (channel == null || channel.isEmpty()) return;

        String channelName = event.getChannel().getName();
        if (!channel.contains(channelName)) return;

        String nickname = event.getUser().getName();
        String message = event.getMessage();

        Pair<String, String> reply = null;
        if (event.getReplyInfo() != null) {
            ChatReply chatReply = event.getReplyInfo();

            // remove @ping from message
            int firstSpaceIndex = message.indexOf(' ');
            if (firstSpaceIndex != -1) {
                message = message.substring(firstSpaceIndex).trim();
            }

            reply = Pair.of(chatReply.getThreadUserName(), chatReply.getMessageBody());
        }

        twitchSender.sendMessage(nickname, channelName, message, reply);
    }

}
