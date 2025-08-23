package net.flectone.pulse.model.event.metadata;

import lombok.Getter;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.util.constant.MessageType;

@Getter
public class ChatMessageMetadata extends MessageMetadata {

    private final Message.Chat.Type chatMessageType;
    private final String messageTypeValue;

    public ChatMessageMetadata(String messageContents, Message.Chat.Type chatMessageType, String messageTypeValue) {
        super(MessageType.CHAT, messageContents);

        this.chatMessageType = chatMessageType;
        this.messageTypeValue = messageTypeValue;
    }
}
