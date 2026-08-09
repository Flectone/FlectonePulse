package net.flectone.pulse.module.command.tell;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.event.message.context.StringMessageContext;
import net.flectone.pulse.model.value.Range;
import net.flectone.pulse.module.command.tell.listener.PulseTellListener;
import net.flectone.pulse.module.command.tell.listener.TellProxyMessageListener;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.DisableSender;
import net.flectone.pulse.platform.sender.IgnoreSender;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import org.incendo.cloud.context.CommandContext;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TellModuleImpl implements TellModule {

    private final Map<UUID, String> senderReceiverMap = new ConcurrentHashMap<>();

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final ProxySender proxySender;
    private final SocialService socialService;
    private final CommandParserProvider commandParserProvider;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final IgnoreSender ignoreSender;
    private final DisableSender disableSender;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;
    private final ListenerRegistry listenerRegistry;
    private final ProxyRegistry proxyRegistry;

    @Override
    public void onEnable() {
        String promptPlayer = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::player);
        String promptMessage = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::message);
        commandModuleController.registerCommand(this, manager -> manager
                .required(promptPlayer, commandParserProvider.playerParser(config().suggestOfflinePlayers()))
                .required(promptMessage, commandParserProvider.nativeMessageParser())
                .permission(permission().name())
        );

        if (proxyRegistry.hasEnabledProxy()) {
            listenerRegistry.register(TellProxyMessageListener.class);
        }

        listenerRegistry.register(PulseTellListener.class);
    }

    @Override
    public void onDisable() {
        senderReceiverMap.clear();
        commandModuleController.clearPrompts(this);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        String playerName = commandModuleController.getArgument(this, commandContext, 0);
        String message = commandModuleController.getArgument(this, commandContext, 1);

        send(fPlayer, playerName, message);
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_TELL;
    }

    @Override
    public Command.Tell config() {
        return fileFacade.command().tell();
    }

    @Override
    public Permission.Command.Tell permission() {
        return fileFacade.permission().command().tell();
    }

    @Override
    public Localization.Command.Tell localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().tell();
    }

    @Override
    public void send(FPlayer fPlayer, String playerName, String message) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        if (fPlayer.name().equalsIgnoreCase(playerName)) {
            messageDispatcher.dispatch(this, EventMetadata.builder()
                    .destination(config().destination())
                    .messageContext(fResolver -> StringMessageContext.builder()
                            .base(MessageContext.builder()
                                    .sender(fPlayer)
                                    .receiver(fResolver)
                                    .message(localization(fResolver).myself())
                                    .tagResolver(messagePipeline.messageTag(fPlayer, fResolver, message))
                                    .build()
                            )
                            .string(message)
                            .build()
                    )
                    .build()
            );

            return;
        }

        Range range = config().range();
        FPlayer fReceiver = fPlayerService.getFPlayer(playerName);

        if (!fReceiver.isConsole()
                && (fReceiver.isUnknown() || !fReceiver.isOnline() || !socialService.canSeeVanished(fReceiver, fPlayer)
                || !range.is(Range.Type.PROXY) && !platformPlayerAdapter.isOnline(fReceiver))) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).nullPlayer())
                            .build()
                    )
                    .build()
            );

            return;
        }

        if (ignoreSender.sendIfIgnored(fPlayer, fReceiver)) return;
        if (disableSender.sendIfDisabled(fPlayer, fReceiver, name())) return;

        // save for sender
        saveReceiver(fPlayer.uuid(), fReceiver.name());

        if (!fPlayer.isConsole() && !fReceiver.isConsole()) {
            String receiverUUID = fReceiver.uuid().toString();

            UUID metadataUUID = UUID.randomUUID();
            boolean isSent = proxySender.send(fPlayer, name(), dataOutputStream -> {
                dataOutputStream.writeUTF(receiverUUID);
                dataOutputStream.writeUTF(message);
            }, metadataUUID);

            if (isSent) {
                send(fPlayer, fReceiver, fPlayer, Localization.Command.Tell::sender, message, metadataUUID);
                return;
            }
        }

        send(fPlayer, fReceiver, fPlayer, Localization.Command.Tell::sender, message, UUID.randomUUID());
        send(fPlayer, fReceiver, fReceiver, Localization.Command.Tell::receiver, message, UUID.randomUUID());
    }

    @Override
    public void send(FEntity sender,
                     FPlayer target,
                     FPlayer fReceiver,
                     Function<Localization.Command.Tell, String> format,
                     String string,
                     UUID metadataUUID) {
        boolean isSenderToSender = sender.equals(fReceiver);

        messageDispatcher.dispatch(this, EventMetadata.builder()
                .filter(fReceiver)
                .destination(config().destination())
                .sound(isSenderToSender ? null : soundOrThrow())
                .messageContext(fResolver -> StringMessageContext.builder()
                        .base(MessageContext.builder()
                                .uuid(metadataUUID)
                                .sender(sender)
                                .receiver(fResolver)
                                .message(format.apply(localization(fResolver)))
                                .tagResolvers(messagePipeline.messageTag(sender, fResolver, string), messagePipeline.targetTag(fResolver, target))
                                .build()
                        )
                        .string(string)
                        .build()
                )
                .build()
        );

        if (!isSenderToSender) {
            saveReceiver(fReceiver.uuid(), sender.name());
        }
    }

    @Override
    public void saveReceiver(UUID player, String receiver) {
        senderReceiverMap.put(player, receiver);
    }

    @Override
    public void removeReceiver(FPlayer fPlayer) {
        senderReceiverMap.remove(fPlayer.uuid());
    }

    @Override
    public @Nullable String getReceiverFor(FPlayer fPlayer) {
        return senderReceiverMap.get(fPlayer.uuid());
    }

}