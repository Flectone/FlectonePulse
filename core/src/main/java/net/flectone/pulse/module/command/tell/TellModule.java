package net.flectone.pulse.module.command.tell;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.Range;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

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
    private final CommandRegistry commandRegistry;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Inject
    public TellModule(FileResolver fileResolver,
                      FPlayerService fPlayerService,
                      ProxySender proxySender,
                      IntegrationModule integrationModule,
                      CommandRegistry commandRegistry,
                      PlatformPlayerAdapter platformPlayerAdapter) {
        super(localization -> localization.getCommand().getTell(), fPlayer -> fPlayer.isSetting(FPlayer.Setting.TELL));

        this.command = fileResolver.getCommand().getTell();
        this.permission = fileResolver.getPermission().getCommand().getTell();
        this.fPlayerService = fPlayerService;
        this.proxySender = proxySender;
        this.integrationModule = integrationModule;
        this.commandRegistry = commandRegistry;
        this.platformPlayerAdapter = platformPlayerAdapter;
    }

    @Override
    protected boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String commandName = getName(command);
        String promptPlayer = getPrompt().getPlayer();
        String promptMessage = getPrompt().getMessage();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .required(promptPlayer, commandRegistry.playerParser(command.isSuggestOfflinePlayers()))
                        .required(promptMessage, commandRegistry.nativeMessageParser())
                        .permission(permission.getName())
                        .handler(this)
        );
    }

    @Override
    public void onDisable() {
        senderReceiverMap.clear();
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableAction.YOU)) return;
        if (checkMute(fPlayer)) return;

        String promptPlayer = getPrompt().getPlayer();
        String playerName = commandContext.get(promptPlayer);

        String promptMessage = getPrompt().getMessage();
        String message = commandContext.get(promptMessage);

        send(fPlayer, playerName, message);
    }

    public void send(FPlayer fPlayer, String playerName, String message) {
        if (checkModulePredicates(fPlayer)) return;

        if (fPlayer.getName().equalsIgnoreCase(playerName)) {
            builder(fPlayer)
                    .format(Localization.Command.Tell::getMyself)
                    .message(message)
                    .sendBuilt();
            return;
        }

        int range = command.getRange();
        FPlayer fReceiver = fPlayerService.getFPlayer(playerName);

        if (fReceiver.isUnknown()
                || !fReceiver.isOnline()
                || !rangeFilter(fPlayer, range).test(fReceiver)
                || range != Range.PROXY && !platformPlayerAdapter.isOnline(fReceiver)) {
            builder(fPlayer)
                    .format(Localization.Command.Tell::getNullPlayer)
                    .sendBuilt();
            return;
        }

        fPlayerService.loadIgnores(fPlayer);

        if (checkIgnore(fPlayer, fReceiver)) return;
        if (checkDisable(fPlayer, fReceiver, DisableAction.HE)) return;

        String receiverUUID = fReceiver.getUuid().toString();

        boolean isSent = proxySender.send(fPlayer, MessageTag.COMMAND_TELL, dataOutputStream -> {
            dataOutputStream.writeUTF(receiverUUID);
            dataOutputStream.writeUTF(message);

            send(fReceiver, fPlayer, (fResolver, s) -> s.getSender(), message);
        });

        if (isSent) return;

        FPlayer fNewReceiver = fPlayerService.getFPlayer(fReceiver.getUuid());
        if (!integrationModule.isVanishedVisible(fNewReceiver, fPlayer)) {
            builder(fPlayer)
                    .format(Localization.Command.Tell::getNullPlayer)
                    .sendBuilt();
            return;
        }

        send(fPlayer, fNewReceiver, (fResolver, s) -> s.getReceiver(), message);
        send(fNewReceiver, fPlayer, (fResolver, s) -> s.getSender(), message);
    }

    public void send(FEntity fPlayer, FPlayer fReceiver, BiFunction<FPlayer, Localization.Command.Tell, String> format, String string) {
        builder(fPlayer)
                .destination(command.getDestination())
                .receiver(fReceiver)
                .format(format)
                .message(string)
                .sound(getSound())
                .sendBuilt();

        senderReceiverMap.put(fReceiver.getUuid(), fPlayer.getName());
    }
}