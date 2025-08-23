package net.flectone.pulse.module.message.chat.model.metadata;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.model.event.EventMetadata;

/**
 * @param messageContents Message contents are only used when the user specifically types out a message
 */
public record ChatMetadata(String messageContents,
                           Message.Chat.Type chatMessageType,
                           String messageTypeValue) implements EventMetadata {

}
