package net.flectone.pulse.module.message.vanilla;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.protocol.packets.interface_.ServerMessage;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.IntegrationMessageFormat;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.value.Range;
import net.flectone.pulse.module.message.vanilla.extractor.HytaleComponentExtractor;
import net.flectone.pulse.module.message.vanilla.listener.HytaleDeathListener;
import net.flectone.pulse.module.message.vanilla.model.ParsedComponent;
import net.flectone.pulse.module.message.vanilla.model.VanillaMessageContext;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.HytaleListenerRegistry;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

@Singleton
public class HytaleVanillaModule extends VanillaModuleImpl {

    private final HytaleComponentExtractor extractor;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final TaskScheduler taskScheduler;
    private final ModuleController moduleController;
    private final SocialService socialService;
    private final Gson gson;

    @Inject
    public HytaleVanillaModule(FileFacade fileFacade,
                               ProxyRegistry proxyRegistry,
                               ListenerRegistry listenerRegistry,
                               HytaleComponentExtractor extractor,
                               MessagePipeline messagePipeline,
                               MessageDispatcher messageDispatcher,
                               FPlayerService fPlayerService,
                               TaskScheduler taskScheduler,
                               HytaleListenerRegistry hytaleListenerRegistry,
                               HytaleDeathListener deathListener,
                               ModuleController moduleController,
                               SocialService socialService,
                               Gson gson) {
        super(fileFacade, proxyRegistry, listenerRegistry, socialService);

        this.extractor = extractor;
        this.messagePipeline = messagePipeline;
        this.messageDispatcher = messageDispatcher;
        this.taskScheduler = taskScheduler;
        this.moduleController = moduleController;
        this.socialService = socialService;
        this.gson = gson;

        hytaleListenerRegistry.register(javaPlugin -> javaPlugin.getEntityStoreRegistry().registerSystem(deathListener));

        hytaleListenerRegistry.registerOutboundFilter((playerRef, packet) -> {
            if (!moduleController.isEnable(this)) return false;

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
        taskScheduler.runAsync(() -> privateSend(fPlayer, parsedComponent));
    }

    private void privateSend(FPlayer fPlayer, ParsedComponent parsedComponent) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        Range range = parsedComponent.vanillaMessage().range();
        String vanillaMessageName = parsedComponent.vanillaMessage().name();

        boolean vanished = socialService.isVanished(fPlayer);
        messageDispatcher.dispatch(this, EventMetadata.builder()
                .range(range)
                .filter(fResolver -> vanillaMessageName.isEmpty() || socialService.isSetting(fResolver, vanillaMessageName))
                .filter(fResolver -> socialService.canSeeVanished(fPlayer, fResolver, vanished))
                .destination(parsedComponent.vanillaMessage().destination())
                .messageContext(fResolver -> VanillaMessageContext.builder()
                        .base(MessageContext.builder()
                                .sender(fPlayer)
                                .receiver(fResolver)
                                .message(StringUtils.defaultString(localization(fResolver).types().get(parsedComponent.translationKey())))
                                .tagResolver(argumentTag(fResolver, parsedComponent))
                                .build()
                        )
                        .parsedComponent(parsedComponent)
                        .fakeMessage(false)
                        .vanished(vanished)
                        .build()
                )
                .integration(() -> IntegrationMessageFormat.builder()
                        .messageNames(StringUtils.isNotEmpty(vanillaMessageName)
                                ? List.of(vanillaMessageName.toUpperCase(), parsedComponent.translationKey())
                                : List.of(parsedComponent.translationKey())
                        )
                        .build()
                )
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeUTF(parsedComponent.translationKey());
                    dataOutputStream.writeUTF(gson.toJson(parsedComponent.arguments()));
                    dataOutputStream.writeBoolean(vanished);
                })
                .build()
        );
    }

    @Override
    public TagResolver argumentTag(FPlayer fResolver, ParsedComponent parsedComponent) {
        return messagePipeline.resolver(ARGUMENT, (argumentQueue, _) -> {
            if (!argumentQueue.hasNext()) return MessagePipeline.ReplacementTag.emptyTag();

            OptionalInt numberArgument = argumentQueue.pop().asInt();
            if (numberArgument.isEmpty()) return MessagePipeline.ReplacementTag.emptyTag();

            int number = numberArgument.getAsInt();
            if (number > parsedComponent.arguments().size()) return MessagePipeline.ReplacementTag.emptyTag();

            Object replacement = parsedComponent.arguments().get(number);
            if (replacement == null) return MessagePipeline.ReplacementTag.emptyTag();

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
        return messagePipeline.build(MessageContext.builder()
                .sender(fTarget)
                .receiver(fResolver)
                .message(fTarget.type().equals(FPlayer.PLAYER_TYPE)
                        ? localization.formatPlayer()
                        : localization.formatEntity()
                )
                .build()
        );
    }

}
