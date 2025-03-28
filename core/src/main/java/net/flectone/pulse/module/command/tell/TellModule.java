package net.flectone.pulse.module.command.tell;

import lombok.Getter;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.connector.ProxyConnector;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiFunction;

@Getter
public abstract class TellModule extends AbstractModuleCommand<Localization.Command.Tell> {

    private final HashMap<UUID, String> senderReceiverMap = new HashMap<>();

    @Getter private final Command.Tell command;
    @Getter private final Permission.Command.Tell permission;

    private final FPlayerService fPlayerService;
    private final ProxyConnector proxyConnector;
    private final IntegrationModule integrationModule;
    private final CommandUtil commandUtil;

    public TellModule(FileManager fileManager,
                      FPlayerService fPlayerService,
                      ProxyConnector proxyConnector,
                      IntegrationModule integrationModule,
                      CommandUtil commandUtil) {
        super(localization -> localization.getCommand().getTell(), fPlayer -> fPlayer.isSetting(FPlayer.Setting.TELL));

        this.fPlayerService = fPlayerService;
        this.proxyConnector = proxyConnector;
        this.integrationModule = integrationModule;
        this.commandUtil = commandUtil;

        command = fileManager.getCommand().getTell();
        permission = fileManager.getPermission().getCommand().getTell();
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableAction.YOU)) return;
        if (checkMute(fPlayer)) return;

        String playerName = commandUtil.getString(0, arguments);
        String message = commandUtil.getString(1, arguments);

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

        FPlayer fReceiver = fPlayerService.getFPlayer(playerName);
        if (fReceiver.isUnknown() || !fReceiver.isOnline()) {
            builder(fPlayer)
                    .format(Localization.Command.Tell::getNullPlayer)
                    .sendBuilt();
            return;
        }

        fPlayerService.loadIgnores(fPlayer);

        if (checkIgnore(fPlayer, fReceiver)) return;
        if (checkDisable(fPlayer, fReceiver, DisableAction.HE)) return;

        String receiverUUID = fReceiver.getUuid().toString();

        boolean isSent = proxyConnector.sendMessage(fPlayer, MessageTag.COMMAND_TELL, byteArrayDataOutput -> {
            byteArrayDataOutput.writeUTF(receiverUUID);
            byteArrayDataOutput.writeUTF(message);

            send(fReceiver, fPlayer, (fResolver, s) -> s.getSender(), message);
        });

        if (isSent) return;

        FPlayer fNewReceiver = fPlayerService.getFPlayer(fReceiver.getUuid());
        if (integrationModule.isVanished(fNewReceiver)) {
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

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        getCommand().getAliases().forEach(commandUtil::unregister);

        createCommand();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }
}