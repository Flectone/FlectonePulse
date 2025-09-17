package net.flectone.pulse.module.command.tell;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.filter.RangeFilter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.DisableSender;
import net.flectone.pulse.platform.sender.IgnoreSender;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.incendo.cloud.context.CommandContext;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;

@Singleton
public class TellModule extends AbstractModuleCommand<Localization.Command.Tell> {

    @Getter private final HashMap<UUID, String> senderReceiverMap = new HashMap<>();

    private final Command.Tell command;
    private final Permission.Command.Tell permission;
    private final FPlayerService fPlayerService;
    private final ProxySender proxySender;
    private final IntegrationModule integrationModule;
    private final CommandParserProvider commandParserProvider;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final RangeFilter rangeFilter;
    private final IgnoreSender ignoreSender;
    private final DisableSender disableSender;

    @Inject
    public TellModule(FileResolver fileResolver,
                      FPlayerService fPlayerService,
                      ProxySender proxySender,
                      IntegrationModule integrationModule,
                      CommandParserProvider commandParserProvider,
                      PlatformPlayerAdapter platformPlayerAdapter,
                      RangeFilter rangeFilter,
                      IgnoreSender ignoreSender,
                      DisableSender disableSender) {
        super(localization -> localization.getCommand().getTell(), Command::getTell, MessageType.COMMAND_TELL);

        this.command = fileResolver.getCommand().getTell();
        this.permission = fileResolver.getPermission().getCommand().getTell();
        this.fPlayerService = fPlayerService;
        this.proxySender = proxySender;
        this.integrationModule = integrationModule;
        this.commandParserProvider = commandParserProvider;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.rangeFilter = rangeFilter;
        this.ignoreSender = ignoreSender;
        this.disableSender = disableSender;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::getPlayer);
        String promptMessage = addPrompt(1, Localization.Command.Prompt::getMessage);
        registerCommand(manager -> manager
                .required(promptPlayer, commandParserProvider.playerParser(command.isSuggestOfflinePlayers()))
                .required(promptMessage, commandParserProvider.nativeMessageParser())
                .permission(permission.getName())
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

    public void send(FPlayer fPlayer, String playerName, String message) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        if (fPlayer.getName().equalsIgnoreCase(playerName)) {
            sendMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Tell::getMyself)
                    .destination(command.getDestination())
                    .message(message)
                    .build()
            );

            return;
        }

        Range range = command.getRange();
        FPlayer fReceiver = fPlayerService.getFPlayer(playerName);

        if (!fReceiver.isConsole() && (fReceiver.isUnknown() || !fReceiver.isOnline() || !rangeFilter.createFilter(fPlayer, range).test(fReceiver) || !range.is(Range.Type.PROXY) && !platformPlayerAdapter.isOnline(fReceiver))) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Tell::getNullPlayer)
                    .build()
            );

            return;
        }

        fPlayerService.loadIgnoresIfOffline(fReceiver);
        if (ignoreSender.sendIfIgnored(fPlayer, fReceiver)) return;

        fPlayerService.loadSettingsIfOffline(fReceiver);
        if (disableSender.sendIfDisabled(fPlayer, fReceiver, getMessageType())) return;

        FPlayer fNewReceiver = fPlayerService.getFPlayer(fReceiver.getUuid());
        if (!integrationModule.canSeeVanished(fNewReceiver, fPlayer)) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Tell::getNullPlayer)
                    .build()
            );

            return;
        }

        // save for sender
        senderReceiverMap.put(fPlayer.getUuid(), fReceiver.getName());

        if (!fPlayer.isConsole() && !fReceiver.isConsole()) {
            String receiverUUID = fReceiver.getUuid().toString();

            UUID metadataUUID = UUID.randomUUID();
            boolean isSent = proxySender.send(fPlayer, getMessageType(), dataOutputStream -> {
                dataOutputStream.writeUTF(receiverUUID);
                dataOutputStream.writeUTF(message);
            }, metadataUUID);

            if (isSent) {
                send(fPlayer, fReceiver, fPlayer, Localization.Command.Tell::getSender, message, metadataUUID);
                return;
            }
        }

        send(fPlayer, fNewReceiver, fPlayer, Localization.Command.Tell::getSender, message, UUID.randomUUID());
        send(fPlayer, fNewReceiver, fNewReceiver, Localization.Command.Tell::getReceiver, message, UUID.randomUUID());
    }

    public void send(FEntity sender,
                     FPlayer target,
                     FPlayer fReceiver,
                     Function<Localization.Command.Tell, String> format,
                     String string,
                     UUID metadataUUID) {
        boolean isSenderToSender = sender.equals(fReceiver);

        sendMessage(metadataBuilder()
                .uuid(metadataUUID)
                .sender(sender)
                .filterPlayer(fReceiver)
                .format(format)
                .destination(command.getDestination())
                .message(string)
                .sound(isSenderToSender ? null : getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, target)})
                .build()
        );

        if (!isSenderToSender) {
            senderReceiverMap.put(fReceiver.getUuid(), sender.getName());
        }
    }
}