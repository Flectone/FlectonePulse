package net.flectone.pulse.module.command.tell;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.DisableSender;
import net.flectone.pulse.platform.sender.IgnoreSender;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.incendo.cloud.context.CommandContext;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TellModule extends AbstractModuleCommand<Localization.Command.Tell> {

    private final Map<UUID, String> senderReceiverMap = new Object2ObjectOpenHashMap<>();

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final ProxySender proxySender;
    private final IntegrationModule integrationModule;
    private final CommandParserProvider commandParserProvider;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final IgnoreSender ignoreSender;
    private final DisableSender disableSender;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::player);
        String promptMessage = addPrompt(1, Localization.Command.Prompt::message);
        registerCommand(manager -> manager
                .required(promptPlayer, commandParserProvider.playerParser(config().suggestOfflinePlayers()))
                .required(promptMessage, commandParserProvider.nativeMessageParser())
                .permission(permission().name())
        );
    }

    @Override
    public void onDisable() {
        super.onDisable();

        senderReceiverMap.clear();
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        String playerName = getArgument(commandContext, 0);
        String message = getArgument(commandContext, 1);

        send(fPlayer, playerName, message);
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_TELL;
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
    public Localization.Command.Tell localization(FEntity sender) {
        return fileFacade.localization(sender).command().tell();
    }

    public void send(FPlayer fPlayer, String playerName, String message) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        if (fPlayer.name().equalsIgnoreCase(playerName)) {
            sendMessage(EventMetadata.<Localization.Command.Tell>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Tell::myself)
                    .destination(config().destination())
                    .message(message)
                    .build()
            );

            return;
        }

        Range range = config().range();
        FPlayer fReceiver = fPlayerService.getFPlayer(playerName);

        if (!fReceiver.isConsole()
                && (fReceiver.isUnknown() || !fReceiver.isOnline() || !integrationModule.canSeeVanished(fReceiver, fPlayer) || !range.is(Range.Type.PROXY) && !platformPlayerAdapter.isOnline(fReceiver))) {
            sendErrorMessage(EventMetadata.<Localization.Command.Tell>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Tell::nullPlayer)
                    .build()
            );

            return;
        }

        fReceiver = fPlayerService.loadIgnoresIfOffline(fReceiver);
        if (ignoreSender.sendIfIgnored(fPlayer, fReceiver)) return;

        fReceiver = fPlayerService.loadSettingsIfOffline(fReceiver);
        if (disableSender.sendIfDisabled(fPlayer, fReceiver, messageType())) return;

        // save for sender
        senderReceiverMap.put(fPlayer.uuid(), fReceiver.name());

        if (!fPlayer.isConsole() && !fReceiver.isConsole()) {
            String receiverUUID = fReceiver.uuid().toString();

            UUID metadataUUID = UUID.randomUUID();
            boolean isSent = proxySender.send(fPlayer, messageType(), dataOutputStream -> {
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

    public void send(FEntity sender,
                     FPlayer target,
                     FPlayer fReceiver,
                     Function<Localization.Command.Tell, String> format,
                     String string,
                     UUID metadataUUID) {
        boolean isSenderToSender = sender.equals(fReceiver);

        sendMessage(EventMetadata.<Localization.Command.Tell>builder()
                .uuid(metadataUUID)
                .sender(sender)
                .filterPlayer(fReceiver)
                .format(format)
                .destination(config().destination())
                .message(string)
                .sound(isSenderToSender ? null : soundOrThrow())
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, target)})
                .build()
        );

        if (!isSenderToSender) {
            senderReceiverMap.put(fReceiver.uuid(), sender.name());
        }
    }

    public @Nullable String getReceiverFor(FPlayer fPlayer) {
        return senderReceiverMap.get(fPlayer.uuid());
    }

}