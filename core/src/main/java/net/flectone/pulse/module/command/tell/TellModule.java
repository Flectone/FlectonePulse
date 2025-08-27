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
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.DisableSource;
import net.flectone.pulse.util.constant.MessageType;
import org.incendo.cloud.context.CommandContext;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiFunction;

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

    @Inject
    public TellModule(FileResolver fileResolver,
                      FPlayerService fPlayerService,
                      ProxySender proxySender,
                      IntegrationModule integrationModule,
                      CommandParserProvider commandParserProvider,
                      PlatformPlayerAdapter platformPlayerAdapter) {
        super(localization -> localization.getCommand().getTell(), Command::getTell, fPlayer -> fPlayer.isSetting(FPlayer.Setting.TELL), MessageType.COMMAND_TELL);

        this.command = fileResolver.getCommand().getTell();
        this.permission = fileResolver.getPermission().getCommand().getTell();
        this.fPlayerService = fPlayerService;
        this.proxySender = proxySender;
        this.integrationModule = integrationModule;
        this.commandParserProvider = commandParserProvider;
        this.platformPlayerAdapter = platformPlayerAdapter;
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
        if (isModuleDisabledFor(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableSource.YOU)) return;
        if (checkMute(fPlayer)) return;

        String playerName = getArgument(commandContext, 0);
        String message = getArgument(commandContext, 1);

        send(fPlayer, playerName, message);
    }

    public void send(FPlayer fPlayer, String playerName, String message) {
        if (isModuleDisabledFor(fPlayer)) return;

        if (fPlayer.getName().equalsIgnoreCase(playerName)) {
            sendMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Tell::getMyself)
                    .message(message)
                    .build()
            );

            return;
        }

        Range range = command.getRange();
        FPlayer fReceiver = fPlayerService.getFPlayer(playerName);

        if (fReceiver.isUnknown()
                || !fReceiver.isOnline()
                || !rangeFilter(fPlayer, range).test(fReceiver)
                || !range.is(Range.Type.PROXY) && !platformPlayerAdapter.isOnline(fReceiver)) {
            sendMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Tell::getNullPlayer)
                    .build()
            );

            return;
        }

        fPlayerService.loadIgnores(fPlayer);

        if (checkIgnore(fPlayer, fReceiver)) return;
        if (checkDisable(fPlayer, fReceiver, DisableSource.HE)) return;

        String receiverUUID = fReceiver.getUuid().toString();

        boolean isSent = proxySender.send(fPlayer, MessageType.COMMAND_TELL, dataOutputStream -> {
            dataOutputStream.writeUTF(receiverUUID);
            dataOutputStream.writeUTF(message);
        });

        if (isSent) {
            send(fReceiver, fPlayer, (fResolver, s) -> s.getSender(), message, true);
            return;
        }

        FPlayer fNewReceiver = fPlayerService.getFPlayer(fReceiver.getUuid());
        if (!integrationModule.canSeeVanished(fNewReceiver, fPlayer)) {
            sendMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Tell::getNullPlayer)
                    .build()
            );

            return;
        }

        send(fPlayer, fNewReceiver, (fResolver, s) -> s.getReceiver(), message, true);
        send(fNewReceiver, fPlayer, (fResolver, s) -> s.getSender(), message, false);
    }

    public void send(FEntity fPlayer,
                     FPlayer fReceiver,
                     BiFunction<FPlayer, Localization.Command.Tell, String> format,
                     String string,
                     boolean senderColorOut) {
        sendMessage(metadataBuilder()
                .sender(fPlayer)
                .receiver(fReceiver, senderColorOut)
                .format(format)
                .destination(command.getDestination())
                .message(string)
                .sound(senderColorOut ? getModuleSound() : null)
                .build()
        );

        senderReceiverMap.put(fReceiver.getUuid(), fPlayer.getName());
    }
}