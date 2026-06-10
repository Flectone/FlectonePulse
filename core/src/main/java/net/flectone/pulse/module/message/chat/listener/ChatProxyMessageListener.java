package net.flectone.pulse.module.message.chat.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.module.message.chat.model.Chat;
import net.flectone.pulse.module.message.chat.model.ChatMetadata;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ChatProxyMessageListener implements PulseListener {

    private final ChatModule chatModule;
    private final ModuleController moduleController;
    private final MessageDispatcher messageDispatcher;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.MESSAGE_CHAT) return event;
        if (moduleController.isDisabledFor(chatModule, event.sender())) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            String proxyChatName = proxyPayload.readString();
            String message = proxyPayload.readString();

            Message.Chat.Type chatType = chatModule.config().types()
                    .entrySet()
                    .stream()
                    .filter(chat -> chat.getKey().equals(proxyChatName))
                    .findAny()
                    .map(Map.Entry::getValue)
                    .orElse(null);
            if (chatType != null && !chatType.range().is(Range.Type.PROXY)) return event.withProcessed(true);

            Chat playerChat = new Chat(proxyChatName, chatType, chatModule.permission().types().get(proxyChatName));

            messageDispatcher.dispatch(chatModule, ChatMetadata.<Localization.Message.Chat>builder()
                    .base(EventMetadata.<Localization.Message.Chat>builder()
                            .uuid(event.uuid())
                            .sender(event.sender())
                            .format(localization -> localization.types().get(proxyChatName))
                            .range(Range.get(Range.Type.SERVER))
                            .destination(chatType != null ? chatType.destination() : Destination.EMPTY_CHAT)
                            .message(message)
                            .sound(playerChat.sound())
                            .filter(chatModule.permissionFilter(proxyChatName))
                            .build()
                    )
                    .chat(playerChat)
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
