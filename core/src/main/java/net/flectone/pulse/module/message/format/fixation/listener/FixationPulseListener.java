package net.flectone.pulse.module.message.format.fixation.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.fixation.FixationModule;
import net.flectone.pulse.processing.resolver.FileResolver;
import org.jetbrains.annotations.Nullable;

@Singleton
public class FixationPulseListener implements PulseListener {

    private final Message.Format.Fixation message;
    private final FixationModule fixationModule;

    @Inject
    public FixationPulseListener(FileResolver fileResolver,
                                 FixationModule fixationModule) {
        this.message = fileResolver.getMessage().getFormat().getFixation();
        this.fixationModule = fixationModule;
    }

    @Pulse
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (!messageContext.isFlag(MessageFlag.FIXATION)) return;
        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;

        String processedMessage = replace(messageContext.getSender(), messageContext.getMessage());
        messageContext.setMessage(processedMessage);
    }

    private String replace(@Nullable FEntity sender, String string) {
        if (fixationModule.isModuleDisabledFor(sender)) return string;
        if (string.isBlank()) return string;

        if (message.isEndDot() && message.getNonDotSymbols().stream().noneMatch(string::endsWith)) {
            string = string + ".";
        }

        if (message.isFirstLetterUppercase()) {
            string = Character.toUpperCase(string.charAt(0)) + string.substring(1);
        }

        return string;
    }
}
