package net.flectone.pulse.module.message.vanilla;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.protocol.packets.interface_.ServerMessage;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.message.vanilla.extractor.HytaleComponentExtractor;
import net.flectone.pulse.module.message.vanilla.model.ParsedComponent;
import net.flectone.pulse.module.message.vanilla.model.VanillaMetadata;
import net.flectone.pulse.platform.registry.HytaleListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

@Singleton
public class HytaleVanillaModule extends VanillaModule {

    private final HytaleComponentExtractor extractor;
    private final MessagePipeline messagePipeline;
    private final TaskScheduler taskScheduler;

    @Inject
    public HytaleVanillaModule(FileFacade fileFacade,
                               HytaleComponentExtractor extractor,
                               MessagePipeline messagePipeline,
                               FPlayerService fPlayerService,
                               TaskScheduler taskScheduler,
                               HytaleListenerRegistry hytaleListenerRegistry) {
        super(fileFacade);

        this.extractor = extractor;
        this.messagePipeline = messagePipeline;
        this.taskScheduler = taskScheduler;

        hytaleListenerRegistry.registerOutboundFilter((playerRef, packet) -> {
            if (!isEnable()) return false;

            if (packet instanceof ServerMessage chatMessage) {
                Optional<ParsedComponent> parsedComponent = extractor.extract(chatMessage.message);
                if (parsedComponent.isPresent()) {
                    send(fPlayerService.getFPlayer(playerRef.getUuid()), parsedComponent.get());
                    return true;
                }
            }

            return false;
        });
    }

    @Override
    public void onEnable() {
        super.onEnable();

        extractor.reload();
    }

    public void send(FPlayer fPlayer, ParsedComponent parsedComponent) {
        taskScheduler.runRegion(fPlayer, () -> privateSend(fPlayer, parsedComponent));
    }

    private void privateSend(FPlayer fPlayer, ParsedComponent parsedComponent) {
        if (isModuleDisabledFor(fPlayer)) return;

        Range range = parsedComponent.vanillaMessage().range();
        String vanillaMessageName = parsedComponent.vanillaMessage().name();

        sendMessage(VanillaMetadata.<Localization.Message.Vanilla>builder()
                .base(EventMetadata.<Localization.Message.Vanilla>builder()
                        .sender(fPlayer)
                        .format(localization -> StringUtils.defaultString(localization.types().get(parsedComponent.translationKey())))
                        .tagResolvers(fResolver -> new TagResolver[]{argumentTag(fResolver, parsedComponent)})
                        .range(range)
                        .filter(fResolver -> vanillaMessageName.isEmpty() || fResolver.isSetting(vanillaMessageName))
                        .destination(parsedComponent.vanillaMessage().destination())
                        .integration()
                        .proxy(dataOutputStream -> {
                            dataOutputStream.writeString(parsedComponent.translationKey());
                            dataOutputStream.writeAsJson(parsedComponent.arguments());
                        })
                        .build()
                )
                .parsedComponent(parsedComponent)
                .build()
        );
    }

    @Override
    public TagResolver argumentTag(FPlayer fResolver, ParsedComponent parsedComponent) {
        return TagResolver.resolver(ARGUMENT, (argumentQueue, context) -> {
            if (!argumentQueue.hasNext()) return Tag.selfClosingInserting(Component.empty());

            OptionalInt numberArgument = argumentQueue.pop().asInt();
            if (numberArgument.isEmpty()) return Tag.selfClosingInserting(Component.empty());

            int number = numberArgument.getAsInt();
            if (number > parsedComponent.arguments().size()) return Tag.selfClosingInserting(Component.empty());

            Object replacement = parsedComponent.arguments().get(number);

            return argumentResolver(fResolver, replacement);
        });
    }

    private Tag argumentResolver(FPlayer fResolver, Object replacement) {
        return switch (replacement) {
            case FEntity fTarget -> Tag.selfClosingInserting(buildFEntityComponent(fTarget, fResolver));
            case Set<?> entities -> {
                Component component = Component.empty();

                boolean first = true;
                for (Object entity : entities) {
                    if (entity instanceof FEntity fTarget) {
                        component = component
                                .append(first ? Component.empty() : Component.text(", "))
                                .append(buildFEntityComponent(fTarget, fResolver));

                        first = false;
                    }
                }

                yield Tag.selfClosingInserting(component);
            }
            default -> Tag.selfClosingInserting(Component.translatable(String.valueOf(replacement)));
        };
    }

    private Component buildFEntityComponent(FEntity fTarget, FPlayer fResolver) {
        Localization.Message.Vanilla localization = localization(fResolver);
        String formatTarget = fTarget.getType().equals(FPlayer.TYPE)
                ? localization.formatPlayer()
                : localization.formatEntity();

        MessageContext context = messagePipeline.createContext(fTarget, fResolver, formatTarget);
        return messagePipeline.build(context);
    }

}
