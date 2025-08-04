package net.flectone.pulse.module.message.format.image.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.platform.formatter.UrlFormatter;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.image.ImageModule;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.StyleBuilderApplicable;
import net.kyori.adventure.text.minimessage.tag.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Singleton
public class ImagePulseListener implements PulseListener {

    private final Message.Format.Image message;
    private final ImageModule imageModule;
    private final MessagePipeline messagePipeline;
    private final UrlFormatter urlFormatter;

    @Inject
    public ImagePulseListener(FileResolver fileResolver,
                              ImageModule imageModule,
                              MessagePipeline messagePipeline,
                              UrlFormatter urlFormatter) {
        this.message = fileResolver.getMessage().getFormat().getImage();
        this.imageModule = imageModule;
        this.messagePipeline = messagePipeline;
        this.urlFormatter = urlFormatter;
    }

    @Pulse
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (!messageContext.isFlag(MessageFlag.IMAGE)) return;

        FEntity sender = messageContext.getSender();
        if (imageModule.isModuleDisabledFor(sender)) return;

        FPlayer receiver = messageContext.getReceiver();
        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.IMAGE, (argumentQueue, context) -> {
            Tag.Argument argument = argumentQueue.peek();
            if (argument == null) return Tag.selfClosingInserting(Component.empty());

            String link = urlFormatter.toASCII(argument.value());
            if (link.isEmpty()) return Tag.selfClosingInserting(Component.empty());

            Component component;
            try {
                component = imageModule.getImage(link);
            } catch (ExecutionException e) {
                return Tag.selfClosingInserting(Component.empty());
            }

            List<StyleBuilderApplicable> styleBuilderApplicables = new ArrayList<>();
            styleBuilderApplicables.add(HoverEvent.showText(component));
            styleBuilderApplicables.add(ClickEvent.openUrl(link));
            styleBuilderApplicables.add(messagePipeline.builder(sender, receiver, message.getColor())
                    .build()
                    .color()
            );

            return Tag.styling(styleBuilderApplicables.toArray(new StyleBuilderApplicable[]{}));
        });
    }

}
